package idc.storyalbum.layout.model.template;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yonatan on 14/5/2015.
 */
@Data
public class PageTemplate {
    @Setter(AccessLevel.NONE)
    private List<Frame> frames = new ArrayList<>();

    private int width;
    private int height;

}
