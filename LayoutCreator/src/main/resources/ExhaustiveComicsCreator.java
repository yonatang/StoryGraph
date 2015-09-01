package uiComponents_ComicsView;

import helpers.ConfigurationManager;
import helpers.Facts;
import helpers.ImageHelpers;
import helpers.Logger;
import helpers.divisionsFor9Images;
import helpers.divisionsFor12Images;

import java.awt.Dimension;
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

import EntryPoints.ImageManager;
import dataTypes.AlbumsHolder;
import dataTypes.CroppedImageData;
import dataTypes.ComicsLayout;
import dataTypes.DisplayImageData;
import dataTypes.DisplayText;
import dataTypes.FrameDetails;
import dataTypes.Layout;
import dataTypes.StoryNodeData;
import dataTypes.StoryText;
import enums.ELogLevel;

public class ExhaustiveComicsCreator {
    private static final String CLASS_NAME = "comicsCreator";

    private boolean presentAsPoster;
    private boolean presentTextInDifferentFrames;

    private AlbumsHolder albums;
    private StoryText storyText;
    private static List<DisplayImageData> displayImagesList;
    public static HashMap<UUID, DisplayImageData> displayImagesUUIDMap;
    private List<Layout> allPageLayouts;
    private ImageManager imagesManagerInstance;
    HashMap<String, BufferedImage> imagesCache, salientImagesCache;
    private List<String> textList;
    private List<List<Integer>> allArrangments;

    /**
     * These values used when dividing the images randomly, and are calculated when loading the templates
     */
    private int minimumImagesInLayout = 1000, maximumImagesInLayout = -1;

    public ExhaustiveComicsCreator(AlbumsHolder albums, StoryText storyText, ImageManager images, HashMap<String, BufferedImage> imagesCache,
                                   HashMap<String, BufferedImage> salientImagesCache, ArrayList<Layout> layouts, List<List<Integer>> allArrangments) {
        this.albums = albums;
        this.imagesCache = imagesCache;
        this.storyText = storyText;
        this.imagesManagerInstance = images;
        displayImagesUUIDMap = new HashMap<UUID, DisplayImageData>();
        this.salientImagesCache = salientImagesCache;
        this.allPageLayouts = layouts;
        this.allArrangments = allArrangments;

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

    public TreeMap<Double, ComicsLayout> calculate() {
        createDisplayImagesList();
        initLayout()
        if (allPageLayouts.size() == 0) {
            return null;
        }
        textList = storyText.getStoryText(albums.getAlbums().firstEntry().getValue());

        List<Layout> basePageLayout = null;
        ComicsLayout baseStoryComicLayout = new ComicsLayout(), bestComicLayout = null;

        for (List<Integer> currentArrangment : allArrangments) {
            //
            // generatePageLayouts
            //
            basePageLayout = generatePageLayouts(currentArrangment, displayImagesList, false, 0);
            baseStoryComicLayout = new ComicsLayout();
            baseStoryComicLayout.setLayouts(basePageLayout);
            baseStoryComicLayout.addGrades(new LinkedHashSet<>(basePageLayout));
            if (bestComicLayout == null || bestComicLayout.getGrade() < baseStoryComicLayout.getGrade()) {
                bestComicLayout = baseStoryComicLayout;
            }
        }

        comicsLayouts.put(bestComicLayout.getGrade(), bestComicLayout);
        addStoryText(comicsLayouts);
        return comicsLayouts;
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

                boolean layoutForRiddle = false, riddleImageExist = false;
                // choosing the layouts that the number of images in them are like the distribution I have
                StringBuilder allLayoutsDetails = new StringBuilder();
                for (Layout currentLayout : allPageLayouts) {
                    layoutForRiddle = false;
                    riddleImageExist = false;
                    if (currentLayout.getNumOfFrames() == numOfImagesInCurrentLayout) {
                        if (currentLayout.getTextDetails() == null && numOfImagesInCurrentLayout == 1) {
                            layoutForRiddle = true;
                        } else {
                            if (currentLayout.getTextDetails().size() != numOfImagesInCurrentLayout) {
                                layoutForRiddle = true;
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
                            //
                            //calcGradeForLayout
                            //
                            currentlayoutCloned.setGrade(calcGradeForLayout(currentlayoutCloned, startIndex + distributionsStartIndex));
                            potentialLayouts.add(currentlayoutCloned);
                        }
                    }
                }

                if (potentialLayouts.size() > 0) {
                    Layout bestGradedLayout = potentialLayouts.get(0);
                    for (int potentialLayoutsCuonter = 1; potentialLayoutsCuonter < potentialLayouts.size(); potentialLayoutsCuonter++) {
                        if (bestGradedLayout.getGrade() < potentialLayouts.get(potentialLayoutsCuonter).getGrade()) {
                            bestGradedLayout = potentialLayouts.get(potentialLayoutsCuonter);
                        }
                    }
                    allLayoutsDetails.append(String.format("%s, ", bestGradedLayout.getName()));
                    if (bestGradedLayout.getDisplayImages().size() != bestGradedLayout.getBestCroppedImageDataList().size()) {
                        Logger.getInstance().Log(
                                ELogLevel.error,
                                CLASS_NAME,
                                "generatePageLayouts",
                                String.format("Number of cropped(%d) and images(%d) are different!",
                                        bestGradedLayout.getBestCroppedImageDataList().size(),
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
                        fontInitialSize = fontInitialSize > Facts.FONT_MAXIMAL_SIZE ?
                                Facts.FONT_MAXIMAL_SIZE : fontInitialSize < Facts.FONT_MINIMAL_SIZE ?
                                Facts.FONT_MINIMAL_SIZE : fontInitialSize;

                        DisplayText currentDisplayText = StoryText.getDisplayText(currentText,
                                new Dimension(textFrameWidth, textFrameHeight),
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

            newDisplayText = StoryText.getDisplayText(currentText, desiredOverlapingPage.getLayoutDetails().getHeight(),
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
            DisplayImageData currentDisplayImage = ExhaustiveComicsCreator.getDisplayImageByUUID(desiredOverlapingPage.getDisplayImages().get(imagesCounter));
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
        actualJump = 1;// (double) maxSizeToIterate * jumpPercentForSaliencyCropping / 100;

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

        return bestCroppedImageData;
    }


    static DisplayImageData getDisplayImageByUUID(UUID currentDisplayImageUUID) {
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
                for (UUID currentDisplayImageUUID : pageLayout.getDisplayImages()) {
                    getDisplayImageByUUID(currentDisplayImageUUID).setText(allStoryText.get(textCounter));
                    textCounter++;
                }
            }
        }
    }

}
