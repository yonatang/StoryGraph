package idc.storyalbum.model.image;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yonatan on 24/4/2015.
 */
@Data
@NoArgsConstructor
public class Rectangle {
    public Rectangle(Rectangle old) {
        this.x = old.x;
        this.y = old.y;
        this.width = old.width;
        this.height = old.height;
    }

    private int x;
    private int y;
    private int width;
    private int height;

    public Dimension getDimension() {
        return new Dimension(width, height);
    }
}
