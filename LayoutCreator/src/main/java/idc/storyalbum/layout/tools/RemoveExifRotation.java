package idc.storyalbum.layout.tools;

import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by yonatan on 20/2/2016.
 */
public class RemoveExifRotation {
    public static void main(String... args) throws Exception {
//        file:///Users/yonatan/Dropbox/Studies/Story%20Albums/Sets//Zoo/72157603658654812/images/97.jpg
        //file:///Users/yonatan/Dropbox/Studies/Story%20Albums/Sets//Zoo/72157603658654812/images/71.jpg
        String imagePath = "/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Zoo/72157603658654812/images/71.jpg";
        File image = new File(imagePath);
        BufferedImage bufferedImage = ImageIO.read(image);
        String baseName = FilenameUtils.getBaseName(imagePath);
        String extension = FilenameUtils.getExtension(imagePath);
        File dest = new File(image.getParent(), baseName + "_noexif." + extension);
        ImageIO.write(bufferedImage, "jpg", dest);
    }
}
