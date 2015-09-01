package idc.storyalbum.layout.service;

import com.google.common.base.Splitter;
import idc.storyalbum.model.image.AnnotatedImage;
import idc.storyalbum.model.image.Dimension;
import idc.storyalbum.model.image.Rectangle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yonatan on 16/5/2015.
 */
@Service
@Slf4j
public class ImageService {
    private int minFontSize = 10;
    private int maxFontSize = 50;
    private String fontName = "Comic Sans MS";

    @Autowired
    private LegacyVectorService legacyVectorService;

    @AllArgsConstructor
    @Getter
    public class TextImageHolder {
        private int initFontSize;
        private int actualFontSize;
        private int textHeight;
        private int frameHeight;
        private BufferedImage image;
    }

    /**
     * @param text
     * @param pageHeight
     * @param textRect
     * @return The textual image and the height of the text within the image
     */
    @Cacheable("text-image-cache")
    public TextImageHolder getTextImage(String text, int pageHeight, Rectangle textRect) {
        int defaultFontSize = pageHeight / 10;
        BufferedImage image = new BufferedImage(textRect.getWidth(), textRect.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int fontSize = Math.min(Math.max(minFontSize, defaultFontSize), maxFontSize);

        Graphics2D g = image.createGraphics();
        do {
            Font font = new Font(fontName, Font.CENTER_BASELINE, fontSize);
            int spacingBetweenLines = fontSize / 4;
            int xOffset = (int) (fontSize * 1.5);

            FontMetrics fontMetrics = g.getFontMetrics(font);
            int textHeight = fontMetrics.getHeight();
            int actualWidthForText = textRect.getWidth() - (xOffset * 2);
            List<String> lines = getLines(text, actualWidthForText, fontMetrics);
            if (lines == null) {
                fontSize--;
                continue;
            }
            int requiredHeight = (fontMetrics.getHeight() + spacingBetweenLines) * lines.size() + spacingBetweenLines;
            if (requiredHeight > textRect.getHeight()) {
                fontSize--;
                continue;
            }

            g.setColor(Color.white);
            g.fillRect(0, 0, textRect.getWidth(), textRect.getHeight() - 3);

            // Stroke oldStroke = g2d.getStroke();
            g.setColor(Color.black);
            g.setStroke(new BasicStroke(8));
            g.drawRect(0, 0, textRect.getWidth(), textRect.getHeight());
            // g2d.setStroke(oldStroke);
            g.setFont(font);

            int currentHeight = spacingBetweenLines * 5;
            for (String currentLine : lines) {
                g.drawString(currentLine, xOffset, currentHeight);
                currentHeight += spacingBetweenLines + textHeight;
            }
            currentHeight -= spacingBetweenLines;
            g.dispose();
            return new TextImageHolder(defaultFontSize, fontSize, currentHeight, textRect.getHeight(), image);
        } while (true);
    }

    /**
     * @param text
     * @param maxWidth
     * @param fontMetrics
     * @return null if cannot put text into frame
     */
    private List<String> getLines(String text, int maxWidth, FontMetrics fontMetrics) {
        List<String> lines = new ArrayList<>();
        List<String> sentences = Splitter.on('\n').trimResults().omitEmptyStrings().splitToList(text);
        for (String currentSentence : sentences) {
            if (fontMetrics.stringWidth(currentSentence) <= maxWidth) {
                lines.add(currentSentence);
            } else {
                LinkedList<String> words = new LinkedList<>(
                        Splitter.on(' ').trimResults().omitEmptyStrings().splitToList(currentSentence)
                );
                while (!words.isEmpty()) {
                    String sentencePart = "";
                    boolean canContinue;
                    do {
                        String nextWord = words.removeFirst();
                        if (fontMetrics.stringWidth((sentencePart + " " + nextWord).trim()) <= maxWidth) {
                            canContinue = true;
                            sentencePart = (sentencePart + " " + nextWord).trim();
                        } else {
                            canContinue = false;
                            words.addFirst(nextWord);
                        }
                    } while (!words.isEmpty() && canContinue);
                    if (sentencePart.isEmpty()) {
                        //there is a word too large
                        return null;
                    }
                    lines.add(sentencePart);
                }
            }
        }

        return lines;
    }

    public static enum Orientation {
        HORIZONTAL,
        VERTICAL,
        SQUARE
    }


    @Cacheable("raw-image-cache")
    public BufferedImage loadImage(File folder, String file) {
        try {
            BufferedImage image = ImageIO.read(new File(folder, file));
            return image;
        } catch (IOException e) {
            log.error("Cannot read file {} {}", folder, file);
            throw new RuntimeException(e);
        }
    }

    public Orientation getOrientation(BufferedImage image) {
        if (image.getHeight() == image.getWidth()) {
            return Orientation.SQUARE;
        }
        if (image.getHeight() > image.getWidth()) {
            return Orientation.VERTICAL;
        }
        return Orientation.HORIZONTAL;
    }

    public Orientation getOrientation(Rectangle rectangle) {
        if (rectangle.getHeight() == rectangle.getWidth()) {
            return Orientation.SQUARE;
        }
        if (rectangle.getHeight() > rectangle.getWidth()) {
            return Orientation.VERTICAL;
        }
        return Orientation.HORIZONTAL;
    }

    private File getImageSilencyVectorFile(AnnotatedImage annotatedImage) {
        String baseFile = "/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Riddle/Set1/saliencySum";
        String fileName = FilenameUtils.removeExtension(annotatedImage.getImageFilename());
        return new File(baseFile + File.separatorChar + fileName + ".xml");
    }

    private File getImageFaceVectorFile(AnnotatedImage annotatedImage) {
        String baseFile = "/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Riddle/Set1/faces";
        String fileName = FilenameUtils.removeExtension(annotatedImage.getImageFilename());
        return new File(baseFile + File.separatorChar + fileName + ".xml");
    }

    @Cacheable("cropped-image-cache")
    public Pair<BufferedImage, Double> cropImage(AnnotatedImage albumPageImage, BufferedImage image, Dimension targetSize) {
        log.debug("  Original image {}x{}", image.getWidth(), image.getHeight());
        log.debug("  Target image   {}x{}", targetSize.getWidth(), targetSize.getHeight());

        LegacyVectorService.SalientSum vSilency;
        LegacyVectorService.SalientSum vFaces;
        try {
            vSilency = legacyVectorService.readVector(getImageSilencyVectorFile(albumPageImage));
            vFaces = legacyVectorService.readVector(getImageFaceVectorFile(albumPageImage));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        double widthRatio = (double) targetSize.getWidth() / (double) image.getWidth();
        double heightRatio = (double) targetSize.getHeight() / (double) image.getHeight();
        double targetRatio = Math.max(widthRatio, heightRatio);

        log.debug("  Scale ratio {}", targetRatio);
        BufferedImage scaledImage = getScaledImage(image, targetRatio);

        log.debug("  Scaled image {}x{}", scaledImage.getWidth(), scaledImage.getHeight());
        if (targetSize.getHeight() == scaledImage.getHeight() &&
                targetSize.getWidth() == scaledImage.getWidth()) {
            //perfect fit - same ratio
            return new ImmutablePair<>(scaledImage, 1.0);
        }
        if (vFaces.getHorizontalVector().size() != image.getWidth()) {
            log.error("faces horiz vector size {}, image width {}, file {}",
                    vFaces.getHorizontalVector().size(), image.getWidth(), albumPageImage.getImageFilename());
            throw new RuntimeException("Bad horz face vector for " + albumPageImage.getImageFilename());
        }
        if (vFaces.getVerticalVector().size() != image.getHeight()) {
            log.error("faces vert vector size {}, image height {}, file {}",
                    vFaces.getVerticalVector().size(), image.getHeight(), albumPageImage.getImageFilename());
            throw new RuntimeException("Bad vert face vector for " + albumPageImage.getImageFilename());
        }
        if (vSilency.getHorizontalVector().size() != image.getWidth()) {
            log.error("silency horiz vector size {}, image width {}, file {}",
                    vSilency.getHorizontalVector().size(), image.getWidth(), albumPageImage.getImageFilename());
            throw new RuntimeException("Bad horz silency vector for " + albumPageImage.getImageFilename());
        }
        if (vSilency.getVerticalVector().size() != image.getHeight()) {
            log.error("silency vert vector size {}, image height {}, file {}",
                    vSilency.getVerticalVector().size(), image.getHeight(), albumPageImage.getImageFilename());
            throw new RuntimeException("Bad vert silency vector for " + albumPageImage.getImageFilename());
        }

        boolean xDirectionScan = targetSize.getWidth() < scaledImage.getWidth();
        int windowSize = (xDirectionScan ?
                targetSize.getWidth() :
                targetSize.getHeight());

        int nonScaledWindowSize = (int) (windowSize * (1 / targetRatio));
        int maxI = (xDirectionScan ?
                image.getWidth() - nonScaledWindowSize :
                image.getHeight() - nonScaledWindowSize);
        double bestScore = Double.NEGATIVE_INFINITY;
        int bestI = 0;
        double saliencyOrg = legacyVectorService.calcWindow(vSilency, xDirectionScan, 0,
                (xDirectionScan ? image.getWidth() : image.getHeight()) - 1);
        double facesOrg = legacyVectorService.calcWindow(vFaces, xDirectionScan, 0,
                (xDirectionScan ? image.getWidth() : image.getHeight()) - 1);
        log.debug("Faces fit {}, saliency fit {}", facesOrg, saliencyOrg);
        double beta = 0.2;
        for (int i = 0; i < maxI; i++) {
            double sFit = legacyVectorService.calcWindow(vSilency, xDirectionScan, i, i + nonScaledWindowSize);
            double score;
            if (facesOrg > 0) {
                double fFit = legacyVectorService.calcWindow(vFaces, xDirectionScan, i, i + nonScaledWindowSize);
                score = beta * (sFit / saliencyOrg) + (1 - beta) * (fFit / facesOrg);
            } else {
                score = (sFit / saliencyOrg);
            }
//            log.debug("From {} to {} scanning (window {}, maxI {}) - score {}", i, i + nonScaledWindowSize, nonScaledWindowSize, maxI, score);
            if (score > bestScore) {
                bestScore = score;
                bestI = i;
            }
        }
        bestI = (int) (bestI * targetRatio);

        BufferedImage croppedImage;
        if (xDirectionScan) {
            log.debug("Cropping image {}x{} at {},{} with window {},{}", scaledImage.getWidth(), scaledImage.getHeight(),
                    bestI, 0, targetSize.getWidth(), targetSize.getHeight());
            croppedImage = scaledImage.getSubimage(bestI, 0, targetSize.getWidth(), targetSize.getHeight());
        } else {
            log.debug("Cropping image {}x{} at {},{} with window {},{}", scaledImage.getWidth(), scaledImage.getHeight(),
                    0, bestI, targetSize.getWidth(), targetSize.getHeight());
            croppedImage = scaledImage.getSubimage(0, bestI, targetSize.getWidth(), targetSize.getHeight());
        }
        log.debug("Returning image with score {}", bestScore);
        return new ImmutablePair<>(croppedImage, bestScore);
    }

    private BufferedImage getScaledImage(BufferedImage image, double targetRatio) {
        int newWidth = (int) Math.round(image.getWidth() * targetRatio);
        int newHeight = (int) Math.round(image.getHeight() * targetRatio);

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return scaledImage;
    }

}
