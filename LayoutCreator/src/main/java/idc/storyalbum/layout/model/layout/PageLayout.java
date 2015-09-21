package idc.storyalbum.layout.model.layout;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yonatan on 14/5/2015.
 */
@Data
public class PageLayout {
    @Setter(AccessLevel.NONE)
    private List<ImageFrame> imageFrames = new ArrayList<>();
    private BufferedImage background;
    private int width;
    private int height;
    private double score;
}
