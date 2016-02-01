package idc.storyalbum.annotations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.HashMultiset;
import idc.storyalbum.model.image.AnnotatedImage;
import idc.storyalbum.model.image.AnnotatedSet;
import idc.storyalbum.model.image.CharacterQuality;
import idc.storyalbum.model.image.Rectangle;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Created by yonatan on 7/1/2016.
 */
public class EnhanceCharacters {
    public static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JodaModule());
        objectMapper.registerModule(new GuavaModule());
        return objectMapper;
    }
    public static void main(String... args) throws Exception{
        ObjectMapper objectMapper=objectMapper();
        File src=new File("/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Riddle/Set5/annotatedSet.json");
        File detectsFile=new File("/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Riddle/detections.json");
        AnnotatedSet set = objectMapper.readValue(src, AnnotatedSet.class);
        List<GuyAnnotation> detects=objectMapper.readValue(detectsFile, new TypeReference<List<GuyAnnotation>>() {
        });
        System.out.println(detects);
        Map<String, GuyAnnotation> detectMap=detects.stream()
                .filter(x->x.getImage_path().contains("/Set5"))
                .collect(toMap(x->FilenameUtils.getName(x.getImage_path()), identity()));
        System.out.println(detectMap);

        for (AnnotatedImage annotatedImage : set.getImages()) {
            GuyAnnotation gAnnotate = detectMap.get(annotatedImage.getImageFilename());
//            System.out.println(gAnnotate);
            int n=gAnnotate.getBoxes().size();
            for (int i = 0; i < n; i++) {
                if (gAnnotate.getConfidence().get(i)>0.8){
                    String charName=gAnnotate.getCharacter_names().get(i);
                    List<Integer> box=gAnnotate.getBoxes().get(i);
                    HashMultiset<String> characterIds = annotatedImage.getCharacterIds();
                    double minScore=Double.MAX_VALUE;
                    String targetCharId="";
                    for (String charId : characterIds.elementSet()) {
                        int levDist = StringUtils.getLevenshteinDistance(charId, charName);
                        double score=(double)levDist/(double)Math.max(charId.length(),charName.length());
                        if (score < minScore) {
                            minScore=score;
                            targetCharId=charId;
                        }
//                        System.out.println("C1 "+charId+" C2 "+charName+" : "+score);
                    }
                    if (minScore>0.3){
                        continue;
                    }
                    System.out.println("Matched "+targetCharId+" to "+charName);
                    CharacterQuality cq=new CharacterQuality();
                    int x=box.get(0),
                            y=box.get(1),
                            w=box.get(2)-x,
                            h=box.get(3)-y;
                    Rectangle rect = new Rectangle();
                    cq.setBox(rect);
                    rect.setX(x);
                    rect.setY(y);
                    rect.setHeight(h);
                    rect.setWidth(w);
                    annotatedImage.getCharQualities().put(targetCharId,cq);
                }
            }
        }
        System.out.println(objectMapper.writeValueAsString(set));
    }
}
