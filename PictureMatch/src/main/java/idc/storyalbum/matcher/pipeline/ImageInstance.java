package idc.storyalbum.matcher.pipeline;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import idc.storyalbum.model.image.AnnotatedImage;
import idc.storyalbum.model.image.ImageQuality;
import idc.storyalbum.model.image.Rectangle;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by yonatan on 1/1/2016.
 */
@Data
@NoArgsConstructor
/**
 * Image instance is an instance of the AnnotatedImage, with only the relevant characters in it.
 * Other characters are ignored.
 */
public class ImageInstance extends AnnotatedImage{

    public ImageInstance(AnnotatedImage annotatedImage, List<Set<String>> relevantGroupCharacterIds) {
        setImageDate(annotatedImage.getImageDate());
        setImageFilename(annotatedImage.getImageFilename());
        setImageQuality(annotatedImage.getImageQuality());
        setLocationId(annotatedImage.getLocationId());
        for (Set<String> group : relevantGroupCharacterIds) {
            getCharacterIds().addAll(group);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageInstance that = (ImageInstance) o;
        return Objects.equals(getImageFilename(), that.getImageFilename()) &&
                Objects.equals(getCharacterIds(), that.getCharacterIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getImageFilename(), getCharacterIds());
    }
}
