package idc.storyalbum.model.image;

import lombok.Data;

/**
 * Created by yonatan on 6/1/2016.
 */
@Data
public class CharacterQuality {
    public enum Facing {
        FRONT, SIDE, BACK, DOWN, UP, BOTTOM;
    }
    private Rectangle box;
    private Facing facing;
}
