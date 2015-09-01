import EntryPoints.ImageManager;
import dataTypes.AlbumsHolder;
import dataTypes.ComicsLayout;
import dataTypes.CroppedImageData;
import dataTypes.DisplayImageData;
import dataTypes.DisplayText;
import dataTypes.FrameDetails;
import dataTypes.Layout;
import dataTypes.StoryNodeData;
import dataTypes.StoryText;
import enums.ELogLevel;
import helpers.ConfigurationManager;
import helpers.Facts;
import helpers.ImageHelpers;
import helpers.Logger;
import helpers.divisionsFor12Images;
import helpers.divisionsFor9Images;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

public class ComicsCreator {
    private static final String CLASS_NAME = "comicsCreator";

    private boolean presentAsPoster;
    private AlbumsHolder albums;
    private StoryText storyText;
    private static List<DisplayImageData> displayImagesList;
    public static HashMap<UUID, DisplayImageData> displayImagesUUIDMap;
    private List<Layout> allPageLayouts;
    private ImageManager imagesManagerInstance;
    HashMap<String, BufferedImage> imagesCache, salientImagesCache;
    // HashMap<String, HashMap<Dimension, DisplayText>> textCache = new HashMap<String, HashMap<Dimension, DisplayText>>();

    // public HashMap<String, HashMap<Dimension, DisplayText>> getTextCache() {
    // return textCache;
    // }
    //
    // public void setTextCache(HashMap<String, HashMap<Dimension, DisplayText>> textCache) {
    // this.textCache = textCache;
    // }

    /**
     * These values used when dividing the images randomly, and are calculated when loading the templates
     */
    private int minimumImagesInLayout = 1000, maximumImagesInLayout = -1;

    public ComicsCreator(AlbumsHolder albums, StoryText storyText, ImageManager images, HashMap<String, BufferedImage> imagesCache,
                         HashMap<String, BufferedImage> salientImagesCache, ArrayList<Layout> layouts) {
        this.albums = albums;
        this.imagesCache = imagesCache;
        this.storyText = storyText;
        this.imagesManagerInstance = images;
        displayImagesUUIDMap = new HashMap<UUID, DisplayImageData>();
        this.salientImagesCache = salientImagesCache;
        this.allPageLayouts = layouts;

        try {
            String val = ConfigurationManager.getInstance().getData().getPresentAsPoster();
            presentAsPoster = val.equals("true") ? true : false;
        } catch (Exception e) {
            Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, CLASS_NAME, e.getMessage());
            e.printStackTrace();
        }

        try {
            String val = ConfigurationManager.getInstance().getData().getPresentTextInDifferentFrames();
            presentTextInDifferentFrames = val.equals("true") ? true : false;
        } catch (Exception ex) {
            Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, CLASS_NAME, ex.getMessage());
            ex.printStackTrace();
        }

    }

    private List<String> textList;

    private boolean presentTextInDifferentFrames;

    public TreeMap<Double, ComicsLayout> calculate() {
        TreeMap<Double, ComicsLayout> comicsLayouts = new TreeMap<Double, ComicsLayout>();
        String methodName = "calculate";
        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Starting method");
        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Creating display images list");
        createDisplayImagesList();
        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName,
                "Done. Created " + displayImagesList.size() + " display images. Loading layouts from configuration given path.");
        if (!initLayout()) {
            return null;
        }
        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Done. Loaded " + allPageLayouts.size() + " layouts.");
        if (allPageLayouts.size() == 0) {
            Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, ("loaded " + allPageLayouts.size() + " layouts. Cannot continue"));
            return null;
        }
        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Done. Loading text");
        textList = storyText.getStoryText(albums.getAlbums().firstEntry().getValue());
        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Done. Generating all possible matches of pics and layouts.");

        List<Integer> distributions = divideImagesRandomly(displayImagesList.size(), displayImagesList.size(), false);

        List<Layout> basePageLayout = generatePageLayouts(distributions, displayImagesList, false, 0);
        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName,
                "Done. Created " + basePageLayout.size() + " matches. Generating all possible Comic stories.");

        ComicsLayout baseStoryComicLayout = new ComicsLayout();
        baseStoryComicLayout.setLayouts(basePageLayout);
        baseStoryComicLayout.addGrades(new LinkedHashSet<>(basePageLayout));

        baseStoryComicLayout = rearrangeComicsLayouts_Global(displayImagesList.size(), baseStoryComicLayout, distributions, -1, 1);

        comicsLayouts.put(baseStoryComicLayout.getGrade(), baseStoryComicLayout);
        Logger.getInstance().Log(
                ELogLevel.debug,
                CLASS_NAME,
                methodName,
                String.format("In conclusion: Final grade: %f \tarrangment: %s", baseStoryComicLayout.getGrade(),
                        Arrays.toString(baseStoryComicLayout.calcDistributions())));

        addStoryText(comicsLayouts);

        return comicsLayouts;
    }

    /**
     * Based on iterative local search algorithm:
     *
     * In order to dodge local maxima, I am performing this algorithm: Tearing apart triples of layouts and rebuilding. Then calling the rebuild internal.
     * Finally comparing the best result to the new result. If it is better, the comic layout is updating, otherwise keeps on iterating until enough iterations
     * are made. This is how we receive a global maxima and not a local one.
     *
     * @param comicsLayouts
     */
    public ComicsLayout rearrangeComicsLayouts_Global(int totalNumOfImages, ComicsLayout comicsLayout, List<Integer> distributions, int numOfItemsToTear,
                                                      int numberOfTimesToTear) {
        String methodName = "rearrangeComicsLayouts_Global";

        int numberOfBigChanges = 0;
        Random rand = new Random();
        int cleanSweep = 0;
        boolean isCleanSweep;
        int tearingLocation = 0, randomSeed = 0;
        try {
            ComicsLayout tempComicsLayout = (ComicsLayout) comicsLayout.clone();
            List<Integer> tempDistributions = new ArrayList<Integer>(distributions);
            boolean randomNumOfItemsToTear = (numOfItemsToTear == -1);
            while (cleanSweep < Facts.GLOBAL_CHANGES_NUM_OF_ITERATIONS) {
                Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName,
                        String.format("\tStart Global grade: %f \tarrangment: %s", comicsLayout.getGrade(), distributions.toString()));

                tearingLocation = 0;
                randomSeed = 0;
                isCleanSweep = true;
                List<Layout> mergedPages = new ArrayList<Layout>();
                List<Integer> newDistributions = new ArrayList<Integer>();
                List<Layout> newArrangement = new ArrayList<Layout>();
                if (randomNumOfItemsToTear) {
                    numOfItemsToTear = rand.nextInt(tempComicsLayout.getLayouts().size() / 2) + 1;
                }
                for (int counter = 0; counter < numberOfTimesToTear; counter++) {
                    try {
                        newDistributions.clear();
                        newArrangement.clear();
                        mergedPages.clear();
                        try {
                            randomSeed = tempComicsLayout.getLayouts().size() - (numOfItemsToTear - 1);
                            tearingLocation = rand.nextInt(randomSeed <= 0 ? 1 : randomSeed);
                        } catch (Exception ex) {
                            Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName,
                                    "Error selecting the next location to tear. tempComicsLayout != null ? " + tempComicsLayout != null ? "true" : "false");
                            continue;
                        }
                        int itemsToTearMax = Math.min(tearingLocation + numOfItemsToTear, tempComicsLayout.getLayouts().size() - 1);
                        while (itemsToTearMax <= tearingLocation) {
                            randomSeed = tempComicsLayout.getLayouts().size() - (numOfItemsToTear - 1);
                            tearingLocation = rand.nextInt(randomSeed <= 0 ? 1 : randomSeed);
                        }
                        mergedPages.addAll(tempComicsLayout.getLayouts().subList(tearingLocation, itemsToTearMax));
                        while (mergedPages.size() != numOfItemsToTear) {
                            mergedPages.clear();
                            randomSeed = tempComicsLayout.getLayouts().size() - (numOfItemsToTear - 1);
                            tearingLocation = rand.nextInt(randomSeed <= 0 ? 1 : randomSeed);
                            itemsToTearMax = Math.min(tearingLocation + numOfItemsToTear, tempComicsLayout.getLayouts().size() - 1);
                            mergedPages.addAll(tempComicsLayout.getLayouts().subList(tearingLocation, itemsToTearMax));
                        }
                        recreatePagesAndCalcGrade(totalNumOfImages, mergedPages, newArrangement, newDistributions, tearingLocation);

                        tempComicsLayout.updatePages(newArrangement, tearingLocation, itemsToTearMax - tearingLocation);
                        for (int distributionsCounter = itemsToTearMax - 1; distributionsCounter >= tearingLocation; distributionsCounter--) {
                            tempDistributions.remove(distributionsCounter);
                        }
                        tempDistributions.addAll(tearingLocation, newDistributions);
                    } catch (Exception ex) {
                        Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, ex.getMessage());
                        ex.printStackTrace();
                    }
                    if (tempDistributions.equals(distributions)) {
                        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "skipping global, no change in current distribution");
                        tempComicsLayout = (ComicsLayout) comicsLayout.clone();
                        tempDistributions = new ArrayList<Integer>(distributions);

                        if (randomNumOfItemsToTear) {
                            numOfItemsToTear = rand.nextInt(tempComicsLayout.getLayouts().size() / 2) + 1;
                        }
                        counter--;
                        continue;
                    }
                }
                Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName,
                        String.format("\tEnd global grade: %f \tarrangment: %s", tempComicsLayout.getGrade(), tempDistributions.toString()));
                try {
                    rearrangeComicsLayouts_Local(totalNumOfImages, tempComicsLayout, tempDistributions);
                } catch (Exception ex) {
                    Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, ex.getMessage());
                    ex.printStackTrace();
                }
                if (tempComicsLayout.getGrade() - comicsLayout.getGrade() > 0.001) {
                    Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName,
                            String.format("***Global accepted. replacing: \tgrade: %f\t arrangement %s***", comicsLayout.getGrade(), distributions));
                    comicsLayout = tempComicsLayout;
                    distributions = tempDistributions;
                    numberOfBigChanges++;
                    isCleanSweep = false;
                } else {
                    Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Global rejected");
                }
                tempComicsLayout = (ComicsLayout) comicsLayout.clone();
                tempDistributions = new ArrayList<Integer>(distributions);
                if (isCleanSweep) {
                    cleanSweep++;
                } else {
                    cleanSweep = 0;
                }
            }

            Logger.getInstance().Log(
                    ELogLevel.debug,
                    CLASS_NAME,
                    methodName,
                    "method ended. End Grade: " + comicsLayout.getGrade() + ", Final arrangement: " + distributions.toString() + ", numberOfSmallChanges = "
                            + ", numberOfBigChanges = " + numberOfBigChanges);
        } catch (Exception ex) {
            Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, ex.getMessage());
            ex.printStackTrace();
        }

        return comicsLayout;
    }

    /**
     * Trying to improve the given layout: tearing apart couples of layouts and trying to rebuild - checking if the grade has gone up. If so, the comic layout
     * is updating, otherwise keeps on iterating until a clean swift of number of templates used - 1
     *
     * @param comicsLayouts
     */
    private void rearrangeComicsLayouts_Local(int totalNumOfImages, ComicsLayout comicsLayout, List<Integer> distributions) {
        String methodName = "rearrangeComicsLayouts_Local";
        boolean cleanSweep = true;
        int cleanSweepCounter = 0, numOfItemsToTear = 2, numberOfSmallChanges = 0, generalCounter = 0;
        int tearingLocation = 0;
        try {
            Random randLocationToTear = new Random();
            while (cleanSweepCounter < Facts.LOCAL_CHANGES_NUM_OF_ITERATIONS) {
                cleanSweep = true;
                tearingLocation = randLocationToTear.nextInt(comicsLayout.getLayouts().size() - 1);
                generalCounter++;
                int itemsToTearMax = Math.min(tearingLocation + numOfItemsToTear, comicsLayout.getLayouts().size() - 1);

                Double gradeBeforeTear = comicsLayout.getLayouts().get(tearingLocation).getGrade()
                        + comicsLayout.getLayouts().get(tearingLocation + 1).getGrade();
                gradeBeforeTear /= 2;
                List<Layout> mergedPages = comicsLayout.getLayouts().subList(tearingLocation, itemsToTearMax);
                if (mergedPages.size() != numOfItemsToTear) {
                    continue;
                }
                List<Integer> newDistributions = new ArrayList<Integer>();
                List<Layout> newArrangement = new ArrayList<Layout>();
                Double gradeAfterTear = recreatePagesAndCalcGrade(totalNumOfImages, mergedPages, newArrangement, newDistributions, tearingLocation);
                if (gradeAfterTear > gradeBeforeTear) {
                    comicsLayout.updatePages(newArrangement, tearingLocation, numOfItemsToTear);
                    distributions.subList(tearingLocation, tearingLocation + numOfItemsToTear).clear();
                    distributions.addAll(tearingLocation, newDistributions);
                    cleanSweep = false;
                    numberOfSmallChanges++;
                }
                if (cleanSweep) {
                    cleanSweepCounter++;
                } else {
                    cleanSweepCounter = 0;
                }
            }
        } catch (Exception ex) {
            Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, "tearing location: " + tearingLocation + ", exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            Logger.getInstance().Log(
                    ELogLevel.debug,
                    CLASS_NAME,
                    methodName,
                    String.format("\tEnd local grade: %f \tarrangment: %s \tchanges/accepted changes %d/%d", comicsLayout.getGrade(), distributions.toString(),
                            generalCounter, numberOfSmallChanges));
        }
    }

    /**
     *
     * @param mergedPages
     * @return
     */
    private Double recreatePagesAndCalcGrade(int totalNumOfImages, List<Layout> mergedPages, List<Layout> newArrangment, List<Integer> newDistribution,
                                             int distributionsStartIndex) {
        int numOfImages = 0;
        List<DisplayImageData> mergedLayoutImageDetailsList = new ArrayList<DisplayImageData>();
        for (Layout currentPage : mergedPages) {
            numOfImages += currentPage.getNumOfFrames();
            for (UUID currentImageUUID : currentPage.getDisplayImages()) {
                mergedLayoutImageDetailsList.add(getDisplayImageByUUID(currentImageUUID));
            }
        }
        newDistribution.addAll(divideImagesRandomly(totalNumOfImages, numOfImages, true));
        newArrangment.addAll(generatePageLayouts(newDistribution, mergedLayoutImageDetailsList, true, distributionsStartIndex));

        return ComicsLayout.getGradeForLayout(new LinkedHashSet<Layout>(newArrangment));
    }

    /**
     * based on the given distributions and the loaded layouts, choosing the best layout to use (comparing grades) and return a list of the highest graded
     * layouts
     *
     * @param distributions
     *            the way to distribute the images
     * @param silent
     *            whether to log or not
     *
     * @return a list of the highest graded pages with their image ID
     */
    private List<Layout> generatePageLayouts(List<Integer> distributions, List<DisplayImageData> images, boolean silent, int distributionsStartIndex) {
        String methodName = "generatePageLayouts";
        List<UUID> newList = new ArrayList<UUID>();
        List<Layout> potentialLayouts = null;
        int numOfImagesInCurrentLayout = 0;
        List<Layout> retVal = new ArrayList<Layout>();

        try {
            int startIndex = 0;
            // iterating to the number of distributions
            for (int counter = 0; counter < distributions.size(); counter++) {
                numOfImagesInCurrentLayout = distributions.get(counter);

                potentialLayouts = new ArrayList<Layout>();

                // getting the sub list for the current distribution
                startIndex += counter == 0 ? 0 : distributions.get(counter - 1);
                List<DisplayImageData> subList = images.subList(startIndex, startIndex + distributions.get(counter));
                if (!silent) {
                    StringBuilder message = new StringBuilder("handling subList (" + subList.size() + " items), ");
                    for (DisplayImageData currentDisplayImageData : subList) {
                        message.append(currentDisplayImageData.getId() + "(" + currentDisplayImageData.getOrientation() + "), ");
                    }
                    Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, "generatePageLayouts", message.toString());
                }

                boolean layoutForRiddle = false, riddleImageExist = false;
                // choosing the layouts that the number of images in them are like the distribution I have
                for (Layout currentLayout : allPageLayouts) {
                    layoutForRiddle = false;
                    if (currentLayout.getNumOfFrames() == numOfImagesInCurrentLayout) {
                        if (presentAsPoster || presentTextInDifferentFrames) {
                            if (currentLayout.getTextDetails() == null && numOfImagesInCurrentLayout == 1) {
                                layoutForRiddle = true;
                            } else {
                                if (currentLayout.getTextDetails().size() != numOfImagesInCurrentLayout) {
                                    layoutForRiddle = true;
                                }
                            }
                        }
                        newList.clear();
                        List<String> text = new ArrayList<String>();
                        for (DisplayImageData displayImageData : subList) {
                            if (displayImageData.getImagePath().contains(Facts.RIDDLE_IMAGE_NAME)) {
                                riddleImageExist = true;
                            }
                            newList.add(displayImageData.getId());
                            text.add(displayImageData.getImageText());
                        }
                        currentLayout.setTextList(text);
                        Layout currentlayoutCloned = (Layout) currentLayout.clone();
                        currentlayoutCloned.setDisplayImages(newList);
                        if ((layoutForRiddle && riddleImageExist) || (layoutForRiddle == false && riddleImageExist == false)) {
                            currentlayoutCloned.setGrade(calcGradeForLayout(currentlayoutCloned, startIndex + distributionsStartIndex));
                            potentialLayouts.add(currentlayoutCloned);
                        }
                    }
                }

                if (potentialLayouts.size() > 0) {
                    StringBuilder allLayoutsDetails = new StringBuilder();
                    Layout bestGradedLayout = potentialLayouts.get(0);
                    // allLayoutsDetails.append(String.format("Found %d potential layouts: [%s - %f, ", potentialLayouts.size(), bestGradedLayout.getName(),
                    // bestGradedLayout.getGrade()));
                    for (int potentialLayoutsCuonter = 1; potentialLayoutsCuonter < potentialLayouts.size(); potentialLayoutsCuonter++) {
                        allLayoutsDetails.append(String.format("%s - %f, ", potentialLayouts.get(potentialLayoutsCuonter).getName(),
                                potentialLayouts.get(potentialLayoutsCuonter).getGrade()));
                        if (bestGradedLayout.getGrade() < potentialLayouts.get(potentialLayoutsCuonter).getGrade()) {
                            bestGradedLayout = potentialLayouts.get(potentialLayoutsCuonter);
                        }
                    }
                    // Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, allLayoutsDetails.toString() + "]");
                    if (bestGradedLayout.getDisplayImages().size() != bestGradedLayout.getBestCroppedImageDataList().size()) {
                        Logger.getInstance().Log(
                                ELogLevel.error,
                                CLASS_NAME,
                                "generatePageLayouts",
                                String.format("Number of cropped(%d) and images(%d) are different!", bestGradedLayout.getBestCroppedImageDataList().size(),
                                        bestGradedLayout.getDisplayImages().size()));
                    }
                    retVal.add((Layout) bestGradedLayout.clone());
                    // retVal.add(bestGradedLayout);
                } else {
                    Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, "generatePageLayouts", "Did not find templates to match the images!");
                    retVal.add(null);
                }
            }
        } catch (Exception ex) {
            Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, "Error occured while generating Page Layouts: " + ex.getMessage());
            ex.printStackTrace();
        }

        return retVal;
    }

    /**
     * <p>
     * based on the formula: (w1 * number of images) - (w2 * loss of salient because of text overlapping), calculating the salient grade and returning it.
     * </p>
     *
     * @param currentLayout
     *            the layout to calculate is grade
     * @param firstImageInLayout
     *            the first image from the image collection that appear in this layout
     * @return
     */
    private Double calcGradeForLayout(Layout currentLayout, int firstImageInLayout) {
        Double retVal = 0d;

        retVal += Facts.COMIC_TEMPLATE_WEIGHT_OF_IMAGE_COUNT * currentLayout.getNumOfFrames() * currentLayout.getNumOfFrames()
                / (maximumImagesInLayout * maximumImagesInLayout);
        float resultOfLayoutBeforeCropAndText = 0f;
        int imageAndFrameOrientationMatchCounter = 0;
        int framesCounter = 0;
        List<FrameDetails> frames = currentLayout.getFrames();

        for (UUID currentDisplayImageUUID : currentLayout.getDisplayImages()) {
            DisplayImageData currentDisplayImageData = getDisplayImageByUUID(currentDisplayImageUUID);
            imageAndFrameOrientationMatchCounter += (currentDisplayImageData.getOrientation() == frames.get(framesCounter).getOrientation()) ? 1 : 0;
            resultOfLayoutBeforeCropAndText += (Facts.SALIENT_GRADE_WEIGHT * currentDisplayImageData.getSalientGrade() + Facts.FACES_GRADE_WEIGHT
                    * currentDisplayImageData.getFacesGrade());
            framesCounter++;
        }
        retVal += Facts.COMIC_TEMPLATE_WEIGHT_OF_MATCH_BETWEEN_IMAGE_FRAME_ORIENTATION
                * ((float) imageAndFrameOrientationMatchCounter / currentLayout.getNumOfFrames());
        float resultOfLayout = 0f;
        try {
            resultOfLayout = getLayoutGrade(currentLayout, firstImageInLayout, frames);
        } catch (Exception ex) {
            Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, "calcGradeForLayout", "Error occured during calculating the layout grade: " + ex.toString());
            ex.printStackTrace();
        }
        retVal += Facts.COMIC_TEMPLATE_WEIGHT_OF_DISCOVERED_SALIENT_REGION * resultOfLayout / resultOfLayoutBeforeCropAndText
                / currentLayout.getDisplayImages().size();

        // now add the result of the text - in case that the text is part of the frame that is designated to the image, all we have to do is to get its grade.
        // otherwise need to calculate it..
        double textMatchingGrade = 0f;
        int countOfTextFrames = currentLayout.getDisplayTextList().size();
        if (!presentTextInDifferentFrames && !presentAsPoster) {
            for (DisplayText currentDisplayText : currentLayout.getDisplayTextList()) {
                if (currentDisplayText.getText().contains(Facts.RIDDLE_HINT)) {
                    countOfTextFrames--;
                    System.out.println();
                    continue;
                }
                textMatchingGrade += (double) currentDisplayText.getTextMatchingGrade();
            }
        } else {
            List<FrameDetails> textsAsFrames = currentLayout.getTextDetails();
            List<String> text = currentLayout.getTextList();
            if (textsAsFrames != null) {
                int textCounter = 0;
                for (int counter = 0; counter < textsAsFrames.size(); counter++) {
                    try {
                        FrameDetails currentFrameDetails = textsAsFrames.get(counter);
                        int textFrameHeight = currentFrameDetails.getHeight(), textFrameWidth = currentFrameDetails.getWidth();
                        String currentText = currentLayout.getTextList().get(textCounter);
                        if (currentText.contains(Facts.RIDDLE_HINT)) {
                            textCounter++;
                            currentText = text.get(textCounter);
                        }
                        textCounter++;
                        int fontInitialSize = (int)(currentLayout.getLayoutDetails().getHeight() * Facts.FONT_PERCENT_FOR_DEFAULT);
                        fontInitialSize = fontInitialSize > Facts.FONT_MAXIMAL_SIZE ? Facts.FONT_MAXIMAL_SIZE : fontInitialSize < Facts.FONT_MINIMAL_SIZE ? Facts.FONT_MINIMAL_SIZE : fontInitialSize;

                        DisplayText currentDisplayText = StoryText.getDisplayText(currentText, new Dimension(textFrameWidth, textFrameHeight),
                                fontInitialSize, textFrameWidth > textFrameHeight, true);
                        textMatchingGrade += (double) currentDisplayText.getTextMatchingGrade();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        retVal += (double) Facts.FONT_MATCHING_WEIGHT * textMatchingGrade / countOfTextFrames;

        return retVal;
    }

    private List<DisplayText> getDisplayText(Layout desiredOverlapingPage, int firstImageInLayout, List<FrameDetails> frames, boolean textIsHorizontal,
                                             List<String> text) {
        List<DisplayText> retVal = new ArrayList<DisplayText>();
        for (int counter = firstImageInLayout; counter < desiredOverlapingPage.getNumOfFrames() + firstImageInLayout; counter++) {
            String currentText = null;
            if (text != null) {
                currentText = text.get(counter - firstImageInLayout);
            } else {
                currentText = textList.get(counter);
            }
            DisplayText newDisplayText = null;
            newDisplayText = StoryText.getDisplayText(currentText,desiredOverlapingPage.getLayoutDetails().getHeight(),
                    new Dimension(frames.get(counter - firstImageInLayout).getWidth(), frames.get(counter - firstImageInLayout).getHeight()), textIsHorizontal);
            retVal.add(newDisplayText);
        }

        return retVal;
    }

    private float getLayoutGrade(Layout desiredOverlapingPage, int firstImageInLayout, List<FrameDetails> frames) {
        String methodName = "getLayoutGrade";

        float retVal = 0f;
        // the x and y coordinate for the sub image
        int xCoordinate = 0, yCoordinate = 0;

        // for the best sub image, keeping the salient grade and the sub image. keeping the current salient grade for the other
        CroppedImageData bestCroppedImageData = new CroppedImageData();

        // the new height and width for the sub image
        int newSalientImageHeight = 0, newSalientImageWidth = 0;
        // the current image to load and the calculated sub images
        BufferedImage currentImage;
        // every text is calculated twice - one for horizontal and one for vertical.
        // Then, I decide which one to choose based on the best salient grade returned from the image new scale
        List<String> text = desiredOverlapingPage.getTextList();
        List<DisplayText> displayTextForThisTemplageHorizontal = getDisplayText(desiredOverlapingPage, firstImageInLayout, frames, true, text);
        List<DisplayText> displayTextForThisTemplageVertical = getDisplayText(desiredOverlapingPage, firstImageInLayout, frames, false, text);

        List<DisplayText> finalDisplayTexts = new ArrayList<DisplayText>();

        DisplayText textOfCurrentImage = null;
        for (int imagesCounter = 0; imagesCounter < desiredOverlapingPage.getNumOfFrames(); imagesCounter++) {

            bestCroppedImageData = new CroppedImageData();
            xCoordinate = 0;
            yCoordinate = 0;
            newSalientImageHeight = 0;
            newSalientImageWidth = 0;
            FrameDetails currentFrame = frames.get(imagesCounter);
            DisplayImageData currentDisplayImage = ComicsCreator.getDisplayImageByUUID(desiredOverlapingPage.getDisplayImages().get(imagesCounter));
            CroppedImageData bestCroppedImageDataHorizontal = new CroppedImageData(), bestCroppedImageDataVertical = new CroppedImageData();

            currentImage = imagesCache.get(currentDisplayImage.getImagePath());
            int originalImageWidth = currentImage.getWidth(), originalImageHeight = currentImage.getHeight();
            double ratioOfFrame = 0;
            double newImageWidth, newImageHeight;
            for (int orientationCounter = 0; orientationCounter < 2; orientationCounter++) {

                int frameActualHeight = currentFrame.getHeight();
                int frameActualWidth = currentFrame.getWidth();
                if (orientationCounter % 2 == 0) {
                    textOfCurrentImage = displayTextForThisTemplageHorizontal.get(imagesCounter);
                    if (currentDisplayImage.getImagePath().endsWith(Facts.RIDDLE_IMAGE_NAME)) {
                        textOfCurrentImage = null;
                    }
                    frameActualHeight = currentFrame.getHeight() - (textOfCurrentImage != null ? textOfCurrentImage.getTextAsImage().getHeight() : 0);
                } else {
                    textOfCurrentImage = displayTextForThisTemplageVertical.get(imagesCounter);
                    if (currentDisplayImage.getImagePath().endsWith(Facts.RIDDLE_IMAGE_NAME)) {
                        textOfCurrentImage = null;
                    }
                    frameActualWidth = currentFrame.getWidth() - (textOfCurrentImage != null ? textOfCurrentImage.getTextAsImage().getWidth() : 0);
                }

                ratioOfFrame = (double) frameActualHeight / frameActualWidth;
                newImageWidth = (double) originalImageHeight / ratioOfFrame;
                newImageHeight = (double) originalImageWidth * ratioOfFrame;

                if (newImageWidth == originalImageWidth && newImageHeight == originalImageHeight) {
                    double salientGradeForCurrentImage = Facts.SALIENT_GRADE_WEIGHT * currentDisplayImage.getSalientGrade();
                    salientGradeForCurrentImage += Facts.FACES_GRADE_WEIGHT * currentDisplayImage.getFacesGrade();
                    retVal += salientGradeForCurrentImage;
                    desiredOverlapingPage.addBestCroppedImageDataList(new CroppedImageData(salientGradeForCurrentImage, 0, 0, originalImageWidth,
                            originalImageHeight, ratioOfFrame, new Dimension(frameActualWidth, frameActualHeight), currentDisplayImage.getId()));
                    continue;
                }
                if (newImageWidth > originalImageWidth) {
                    newSalientImageWidth = originalImageWidth;
                    newSalientImageHeight = (int) newImageHeight;
                    if (Math.abs((double) newSalientImageHeight / newSalientImageWidth - ratioOfFrame) > 0.1) {
                        Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, "We did not get the same ratio!! (using new width)");
                    }
                } else {
                    newSalientImageWidth = (int) newImageWidth;
                    newSalientImageHeight = originalImageHeight;
                    if (Math.abs((double) newSalientImageHeight / newSalientImageWidth - ratioOfFrame) > 0.1) {
                        Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, "We did not get the same ratio!! (using new height)");
                    }
                }

                if (orientationCounter % 2 == 0) {
                    bestCroppedImageDataHorizontal = (CroppedImageData) getBestCroppedImageData(newSalientImageHeight == originalImageHeight, ratioOfFrame,
                            frameActualWidth, frameActualHeight, xCoordinate, yCoordinate, originalImageWidth, originalImageHeight, newSalientImageWidth,
                            newSalientImageHeight, currentDisplayImage).clone();
                } else {
                    bestCroppedImageDataVertical = (CroppedImageData) getBestCroppedImageData(newSalientImageHeight == originalImageHeight, ratioOfFrame,
                            frameActualWidth, frameActualHeight, xCoordinate, yCoordinate, originalImageWidth, originalImageHeight, newSalientImageWidth,
                            newSalientImageHeight, currentDisplayImage).clone();
                }
            }
            if (bestCroppedImageDataHorizontal.getSalientGrade() > bestCroppedImageDataVertical.getSalientGrade()) {
                bestCroppedImageData = bestCroppedImageDataHorizontal;
                finalDisplayTexts.add(displayTextForThisTemplageHorizontal.get(imagesCounter));
            } else {
                bestCroppedImageData = bestCroppedImageDataVertical;
                finalDisplayTexts.add(displayTextForThisTemplageVertical.get(imagesCounter));
            }
            retVal += bestCroppedImageData.getSalientGrade();
            desiredOverlapingPage.addBestCroppedImageDataList(bestCroppedImageData);
        }
        desiredOverlapingPage.setDisplayText(finalDisplayTexts);

        return retVal;
    }

    private CroppedImageData getBestCroppedImageData(boolean movingLeftToRight, double ratioOfFrame, int frameWidth, int frameHeight, int x, int y,
                                                     int originalWidth, int originalHeight, int newWidth, int newHeight, DisplayImageData displayImage) {
        String methodName = "getBestCroppedImageData";
        CroppedImageData bestCroppedImageData = new CroppedImageData();
        double currentGrade, actualJump;

        int coordinateToIterate, sizeToIterate, maxSizeToIterate;
        if (movingLeftToRight) {
            coordinateToIterate = x;
            sizeToIterate = newWidth;
            maxSizeToIterate = originalWidth;
        } else {
            coordinateToIterate = y;
            sizeToIterate = newHeight;
            maxSizeToIterate = originalHeight;
        }

        // iterating the image in 1 pixel jumps
        actualJump = 1;

        double totalSalientGrade = displayImage.getSalientGrade();
        double totalFacesGrade = displayImage.getFacesGrade();
        while (coordinateToIterate + sizeToIterate < maxSizeToIterate) {
            try {
                if (movingLeftToRight) {
                    x = coordinateToIterate;
                } else {
                    y = coordinateToIterate;
                }
                double croppedSalientGrade = ((double) ImageHelpers.getGrade(displayImage.getSummedSalientPath(), x, y, newWidth, newHeight,
                        displayImage.getWidth(), displayImage.getHeight()));

                double croppedFacesGrade = ((double) ImageHelpers.getGrade(displayImage.getSummedFacesPath(), x, y, newWidth, newHeight,
                        displayImage.getWidth(), displayImage.getHeight()));
                // System.out.println(String.format("%s, [x,y], [%d,%d], [w,h],[%d,%d], %f", displayImage.getImagePath(), x, y, newWidth, newHeight,
                // croppedFacesGrade));
                currentGrade = (double) (Facts.SALIENT_GRADE_WEIGHT * (double) croppedSalientGrade / totalSalientGrade);
                currentGrade += totalFacesGrade == 0 ? 0 : (double) (Facts.FACES_GRADE_WEIGHT * (double) croppedFacesGrade / totalFacesGrade);

                if (currentGrade > bestCroppedImageData.getSalientGrade()) {
                    bestCroppedImageData = new CroppedImageData(currentGrade, x, y, newWidth, newHeight, ratioOfFrame, new Dimension(frameWidth, frameHeight),
                            displayImage.getId());
                }
            } catch (Exception ex) {
                Logger.getInstance().Log(
                        ELogLevel.error,
                        CLASS_NAME,
                        methodName,
                        String.format("trying to crop image (w,h)=[%d,%d] to (x,y=[%d,%d], w,h=[%d,%d]), ratio %f, error: %s", originalWidth, originalHeight,
                                x, y, newWidth, newHeight, ratioOfFrame, ex.getMessage()));
            }
            coordinateToIterate += actualJump;
        }

        // after the while is done, I have a small width that I did not cover, and this code will do it:
        // xCoordinate -= newSalientImageWidth + xCoordinate - originalSalientImageWidth;
        // if (xCoordinate >= 0 && xCoordinate + newSalientImageWidth <= originalSalientImageWidth) {
        // try { // subImage = currentImage.getSubimage(xCoordinate, yCoordinate, newImageWidth, newImageHeight);
        // subSalientImage = currentSalientImage.getSubimage(xCoordinate, yCoordinate, newSalientImageWidth, newSalientImageHeight);
        // currentSalientGrade = (float) (Facts.salientGradeWeight * (float) ImageHelpers.getSalientGrade(subSalientImage));
        // currentSalientGrade += Facts.faceGradeWeight
        // * (float) getFacesOverlapGrade(currentDisplayImage.getFaces(), xCoordinate, yCoordinate, newSalientImageWidth,
        // newSalientImageHeight);
        // if (currentSalientGrade > bestCroppedImageData.getSalientGrade()) {
        // bestCroppedImageData = new BestCroppedImageData(currentSalientGrade, xCoordinate, yCoordinate, newSalientImageWidth,
        // newSalientImageHeight);
        // }
        // } catch (Exception ex) {
        // Logger.getInstance().Log(
        // ELogLevel.error,
        // CLASS_NAME,
        // methodName,
        // String.format("trying to crop image (w,h=[%d,%d]) to x,y=[%d,%d], w,h=[%d,%d], ratio %f, error: %s",
        // currentSalientImage.getWidth(), currentSalientImage.getHeight(), yCoordinate, xCoordinate, newSalientImageWidth,
        // newSalientImageHeight, ratioOfFrame, ex.getMessage()));
        //
        // }
        // }
        return bestCroppedImageData;
    }

    // private float getFacesOverlapGrade(List<Integer[]> faces, int xCoordinateOfImage, int yCoordinateOfImage, int imageWidth, int imageHeight) {
    // if (faces == null || faces.size() == 0) {
    // return 0f;
    // }
    // float retVal = 0f;
    // for (Integer[] currentFace : faces) {
    // // check if the face is in the image
    // retVal += getFacePercentInImage(currentFace, xCoordinateOfImage, yCoordinateOfImage, imageWidth, imageHeight);
    // }
    //
    // return (float) (retVal / (faces.size() == 0 ? 1 : faces.size()));
    // }
    //
    // /**
    // * For the given face, calculate the % of the face that resides in the image, and returns a grade between 0..1
    // *
    // * @param currentFace
    // * the given face (x, y, width, height)
    // * @param xCoordinateOfImage
    // * the image xCoordinate
    // * @param yCoordinateOfImage
    // * the image yCoordinate
    // * @param imageWidth
    // * the image width
    // * @param imageHeight
    // * the image height
    // * @return a grade between 0..1 representing the % of face that is visible in the given image's x, y and dimensions
    // */
    // private float getFacePercentInImage(Integer[] currentFace, int xCoordinateOfImage, int yCoordinateOfImage, int imageWidth, int imageHeight) {
    // int faceX = currentFace[0], faceY = currentFace[1], faceActualX = faceX, faceActualY = faceY, faceWidth = currentFace[2], faceHeight = currentFace[3],
    // faceWidthInImage = currentFace[2], faceHeightInImage = currentFace[3];
    // if (faceX < xCoordinateOfImage) {
    // faceWidthInImage -= xCoordinateOfImage - faceActualX;
    // faceActualX = xCoordinateOfImage;
    // faceWidthInImage = faceWidthInImage < 0 ? 0 : faceWidthInImage;
    // } else {
    // faceWidthInImage = faceActualX + faceWidth > xCoordinateOfImage + imageWidth ? faceWidth
    // - Math.abs(faceActualX + faceWidth - xCoordinateOfImage - imageWidth) : faceWidth;
    // faceWidthInImage = faceWidthInImage < 0 ? 0 : faceWidthInImage;
    // }
    // if (faceY < yCoordinateOfImage) {
    // faceHeightInImage -= yCoordinateOfImage - faceActualY;
    // faceActualY = yCoordinateOfImage;
    // faceHeightInImage = faceHeightInImage < 0 ? 0 : faceHeightInImage;
    // } else {
    // faceHeightInImage = faceActualY + faceHeight > yCoordinateOfImage + imageHeight ? faceHeight
    // - Math.abs(faceActualY - faceHeight - yCoordinateOfImage - imageHeight) : faceHeight;
    // faceHeightInImage = faceHeightInImage < 0 ? 0 : faceHeightInImage;
    // }
    // if (faceHeightInImage == 0 || faceWidthInImage == 0) {
    // return 0;
    // }
    //
    // if (faceWidthInImage > faceWidth || faceHeightInImage > faceHeight) {
    // Logger.getInstance().Log(
    // ELogLevel.error,
    // CLASS_NAME,
    // "getFacePercentInImage",
    // String.format(
    // "Error! The size of the face within the image is larger than the real size of the head. [w,h]: real [%d,%d], in image [%d,%d]",
    // faceWidth, faceHeight, faceWidthInImage, faceHeightInImage));
    // }
    // return ((float) ((faceActualX + faceWidthInImage) * (faceActualY + faceHeightInImage)) / ((faceX + faceWidth) * (faceY + faceHeight)));
    // }

    // private Double old_calcGradeForLayout(Layout currentLayout) {
    // Double retVal = 0d;
    //
    // retVal += Facts.COMIC_TEMPLATE_WEIGHT_OF_IMAGE_COUNT * currentLayout.getNumOfFrames();
    // int allImagesSize = 0;
    // for (FrameDetails currentLayoutImage : currentLayout.getFrames()) {
    // allImagesSize += currentLayoutImage.getWidth() * currentLayoutImage.getHeight();
    // }
    // Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize().getSize();
    // retVal += Facts.COMIC_TEMPLATE_WEIGHT_OF_IMAGES_TOTAL_SIZE_TO_PAGE_SIZE * (allImagesSize / screenDimension.getHeight() / screenDimension.getWidth());
    // for (UUID currentDisplayImageUUID : currentLayout.getDisplayImages()) {
    // if (getDisplayImageByUUID(currentDisplayImageUUID).getSalientGrade() == 0) {
    //
    // // the original image have been cropped, therefore I should crop the salient image as well and to use this value for grade calculation
    // Double currentSalientGrade = ImageHelpers.getSalientGrade(getDisplayImageByUUID(currentDisplayImageUUID).getSalientImagePath());
    // getDisplayImageByUUID(currentDisplayImageUUID).setSalientGrade(currentSalientGrade);
    // }
    // retVal += Facts.COMIC_TEMPLATE_WEIGHT_OF_DISCOVERED_SALIENT_REGION * getDisplayImageByUUID(currentDisplayImageUUID).getSalientGrade();
    // }
    // return retVal;
    // }

    private static DisplayImageData getDisplayImageByUUID(UUID currentDisplayImageUUID) {
        return displayImagesUUIDMap.get(currentDisplayImageUUID);
    }

    private void createDisplayImagesList() {
        List<StoryNodeData> storyNodes = albums.getAlbums().firstEntry().getValue().getStoryNodes();
        displayImagesList = new ArrayList<DisplayImageData>(storyNodes.size());
        List<String> textOfStory = storyText.getStoryText(albums.getAlbums().firstEntry().getValue());
        for (int counter = 0; counter < storyNodes.size(); counter++) {
            StoryNodeData currentNode = storyNodes.get(counter);
            DisplayImageData newDisplayImageData = new DisplayImageData(imagesManagerInstance.getImageDataByPath(currentNode.getSelectedImagePath()),
                    textOfStory.get(counter));
            displayImagesList.add(newDisplayImageData);
            displayImagesUUIDMap.put(newDisplayImageData.getId(), newDisplayImageData);
        }
    }

    private boolean initLayout() {
        String methodName = "initLayout";

        Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Handling " + (allPageLayouts.size()) + " layouts");
        for (Layout currentPage : allPageLayouts) {
            try {
                int imagesAmount = currentPage.getFrames().size();
                if (imagesAmount < minimumImagesInLayout) {
                    minimumImagesInLayout = imagesAmount;
                }
                if (imagesAmount > maximumImagesInLayout) {
                    maximumImagesInLayout = imagesAmount;
                }
            } catch (Exception ex) {
                Logger.getInstance().Log(ELogLevel.error, CLASS_NAME, methodName, "Failed to handle: " + currentPage + ". Reason: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        return true;
    }

    /**
     * based on the minimum and maximum numbers of images in every layout I randomly distribute number of images
     *
     * @param numOfImages
     *            number of images to distribute
     * @param silence
     *            boolean stating whether the method should log or not
     * @return the random distribution
     */
    private List<Integer> divideImagesRandomly(int numOfImagesInStory, int numOfImages, boolean silence) {
        String methodName = "divideImagesRandomly";
        // if(numOfImages == 9) {
        // return new ArrayList<Integer>(Arrays.asList(1, 2, 2, 2, 2));
        // }
        if (presentAsPoster) {
            if (numOfImages == 9) {
                return new ArrayList<Integer>(Arrays.asList(9));
            } else if (numOfImages == 12) {
                return new ArrayList<Integer>(Arrays.asList(12));
            }
        }
        List<Integer> retVal = new ArrayList<Integer>();
        HashMap<Integer, List<List<Integer>>> allOptions = null;
        if (numOfImagesInStory == 9) {
            divisionsFor9Images division = new divisionsFor9Images();
            allOptions = division.getAllArrangments();
        } else if (numOfImagesInStory == 12) {
            divisionsFor12Images division = new divisionsFor12Images();
            allOptions = division.getAllArrangments();
        }
        Random rand = new Random();
        if (allOptions != null) {
            if (!allOptions.containsKey(numOfImages)) {
                Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Did not find values for num of images: " + numOfImages);
            }
            List<List<Integer>> value = allOptions.get(numOfImages);
            if (value == null || value.size() <= 0) {
                Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "value of all options is null");
            }
            int randVal = value.size() == 1 ? 0 : rand.nextInt(value.size() - 1);
            if (randVal > value.size()) {
                Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, String.format("random value %d > values size %d", randVal, value.size()));
            }
            return value.get(randVal);
        } else {
            int numOfRemainingImages = numOfImages;
            int nextGroupSize = 0;
            while (numOfRemainingImages > minimumImagesInLayout) {
                nextGroupSize = rand.nextInt(maximumImagesInLayout) + 1;
                while (numOfRemainingImages - nextGroupSize < 0) {
                    nextGroupSize = rand.nextInt(maximumImagesInLayout) + minimumImagesInLayout;
                }
                retVal.add(nextGroupSize);
                numOfRemainingImages -= nextGroupSize;
            }
            if (numOfRemainingImages > 0) {
                retVal.add(numOfRemainingImages);
            }
            if (!silence) {
                Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, methodName, "Distribution is: " + retVal.toString());
            }
            return retVal;
        }
    }

    /**
     * Adding the text to every image in every comics layout
     *
     * @param comicsLayouts
     *            the comics layout to add the text to
     */
    private void addStoryText(TreeMap<Double, ComicsLayout> comicsLayouts) {
        List<String> allStoryText = storyText.getStoryText(albums.getAlbums().firstEntry().getValue());
        int textCounter = 0;
        for (Entry<Double, ComicsLayout> currentEntry : comicsLayouts.entrySet()) {
            currentEntry.getValue().setStoryText(storyText);
            textCounter = 0;
            for (int counter = 0; counter < currentEntry.getValue().getLayouts().size(); counter++) {
                Layout pageLayout = currentEntry.getValue().getLayouts().get(counter);
                // try {
                // pageLayout = (PageLayout)currentEntry.getValue().getPages().get(counter).clone();
                // } catch(CloneNotSupportedException e) {
                // Logger.getInstance().Log(ELogLevel.debug, CLASS_NAME, "addStoryText", "failed to clone: " + e.getMessage());
                // }
                for (UUID currentDisplayImageUUID : pageLayout.getDisplayImages()) {
                    getDisplayImageByUUID(currentDisplayImageUUID).setText(allStoryText.get(textCounter));
                    textCounter++;
                }
            }
        }
    }

}
