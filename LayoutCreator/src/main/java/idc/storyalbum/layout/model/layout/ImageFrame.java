package idc.storyalbum.layout.model.layout;

import idc.storyalbum.layout.model.template.Frame;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.image.BufferedImage;

/**
 * Created by yonatan on 15/5/2015.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImageFrame extends Frame {
    private BufferedImage image;
    private BufferedImage textImage;
    private String text;
    private double fitScore;
    private double orientationScore;
    private double textScore;
}
