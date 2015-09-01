package idc.storyalbum.layout.service;

import idc.storyalbum.layout.model.layout.ImageFrame;
import idc.storyalbum.layout.model.layout.Layout;
import idc.storyalbum.layout.model.layout.PageLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by yonatan on 16/5/2015.
 */
@Service
@Slf4j
public class RenderResult {
    public void renderLayout(Layout layout, File folder) throws IOException {
        log.info("Writing output to {}", folder);
        folder.mkdirs();
        int i = 0;
        for (PageLayout pageLayout : layout.getPages()) {
            i++;
            File output = new File(folder, i + ".jpg");
            log.debug("  Creating image {}", output);
            BufferedImage image = new BufferedImage(pageLayout.getWidth(), pageLayout.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            for (ImageFrame imageFrame : pageLayout.getImageFrames()) {
                BufferedImage frameImage=imageFrame.getImage();
                log.debug("    Painting image frame at {},{}",imageFrame.getImageRect().getX(), imageFrame.getImageRect().getY());
                g.drawImage(frameImage, imageFrame.getImageRect().getX(), imageFrame.getImageRect().getY(), null);

                BufferedImage textImage=imageFrame.getTextImage();
                log.debug("    Painting text frame at {},{}",imageFrame.getTextRect().getX(),imageFrame.getTextRect().getY());
                g.drawImage(textImage, imageFrame.getTextRect().getX(), imageFrame.getTextRect().getY(), null);
            }
            g.dispose();
            ImageIO.write(image, "jpg", output);
        }
    }
}
