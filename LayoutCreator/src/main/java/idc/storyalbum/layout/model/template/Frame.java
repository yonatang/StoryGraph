package idc.storyalbum.layout.model.template;

import idc.storyalbum.model.image.Rectangle;
import lombok.Data;

/**
 * Created by yonatan on 14/5/2015.
 */
@Data
public class Frame {
    private Rectangle imageRect=new Rectangle();
    private Rectangle textRect;
}
