package idc.storyalbum.model.album;

import idc.storyalbum.model.graph.StoryEvent;
import idc.storyalbum.model.image.AnnotatedImage;
import idc.storyalbum.model.image.ImageInstance;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yonatan on 22/4/2015.
 */
@Data
@NoArgsConstructor
public class AlbumPage {
    public AlbumPage(ImageInstance image, StoryEvent storyEvent) {
        this.image = image;
        this.storyEvent = storyEvent;
    }

    private ImageInstance image;
    private StoryEvent storyEvent;
    private String text;
}
