package idc.storyalbum.layout.model.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageTemplate {
    @Setter(AccessLevel.NONE)
    private List<Frame> frames = new ArrayList<>();

    private BufferedImage background;
    private int width;
    private int height;

}
