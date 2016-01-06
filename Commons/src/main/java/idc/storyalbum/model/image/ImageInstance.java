package idc.storyalbum.model.image;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    private double crowdedness;

    private HashMultiset<String> relevantCharacters = HashMultiset.create();

    public ImageInstance(AnnotatedImage annotatedImage, List<Set<String>> relevantGroupCharacterIds) {
        setImageDate(annotatedImage.getImageDate());
        setImageFilename(annotatedImage.getImageFilename());
        setImageQuality(annotatedImage.getImageQuality());
        setLocationId(annotatedImage.getLocationId());
        setCharacterIds(annotatedImage.getCharacterIds());
        for (Set<String> group : relevantGroupCharacterIds) {
            getRelevantCharacters().addAll(group);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageInstance that = (ImageInstance) o;
        return Objects.equals(getImageFilename(), that.getImageFilename()) &&
                Objects.equals(getRelevantCharacters(), that.getRelevantCharacters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getImageFilename(), getRelevantCharacters());
    }
}
