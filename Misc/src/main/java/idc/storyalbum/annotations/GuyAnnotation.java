package idc.storyalbum.annotations;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yonatan on 7/1/2016.
 */
@Data
public class GuyAnnotation {
    public static class Box extends ArrayList<Integer>{
    }
    private List<Box> boxes;
    private List<Double> confidence;
    private String image_path;
    private List<String> character_names;

}
