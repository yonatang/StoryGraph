package idc.storyalbum.matcher.pipeline;

import com.google.common.collect.HashMultimap;
import idc.storyalbum.matcher.conf.Props;
import idc.storyalbum.model.graph.Constraint;
import idc.storyalbum.model.graph.StoryEvent;
import idc.storyalbum.model.image.CharacterQuality;
import idc.storyalbum.model.image.ImageInstance;
import idc.storyalbum.model.image.ImageQuality;
import idc.storyalbum.model.image.Rectangle;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by yonatan on 22/4/2015.
 */
@Service
public class ScoreService {

    @Autowired
    private Props.ScoreProps scoreProps;

    public void evictCache() {
        getImagetFitScoreCache.clear();
        getEventScoreCache.clear();
    }

    public double getImageCrowdedness(ImageInstance imageInstance) {
        if (imageInstance.getCharacterIds().size() == 0) {
            return 1.0;
        }
        return (double) imageInstance.getRelevantCharacters().size() / (double) imageInstance.getCharacterIds().size();
    }
    /**
     * Calculate the fineness of a specific image to an event with some random jittering
     *
     * @param image
     * @param event
     * @param nonFuziness
     * @return
     */
    public double getImageFitScore(ImageInstance image, StoryEvent event, double nonFuziness) {
        return getImageFitScore(image, event) + fuziness(nonFuziness);
    }

    private Map<Pair<ImageInstance, StoryEvent>, Double> getImagetFitScoreCache = new HashMap<>();
    private Map<StoryEvent, Double> getEventScoreCache = new HashMap<>();

    /**
     * Calculate the fineness of a specific image to an event
     *
     * @param image
     * @param event
     * @return score
     */
    public double getImageFitScore(ImageInstance image, StoryEvent event) {
        Pair<ImageInstance, StoryEvent> pair = Pair.of(image, event);
        if (getImagetFitScoreCache.containsKey(pair)) {
            return getImagetFitScoreCache.get(pair);
        }
        //calculate soft constraints score
        Set<Constraint> softConstraints = event.getConstraints().stream()
                .filter(Constraint::isSoft)
                .collect(Collectors.toSet());
        double SOFT_CONSTRAINT_FACTOR = 0.5;
        double IMAGE_QUALITY_FACTOR = 0.2;
        double CROWD_FACTOR = 0.1;
        double CHAR_QUALITY_FACTOR = 0.2;

        double softConstraintsScore = 0;
        if (!softConstraints.isEmpty()) {
            long softConstraintsCount = Math.min(softConstraints.size(), 10);
            double factor = (SOFT_CONSTRAINT_FACTOR) / ((double) softConstraintsCount);
            long matchedConstraints = softConstraints.stream()
                    .filter(constraint -> ConstraintUtils.isMatch(constraint, image))
                    .count();
            softConstraintsScore = factor * Math.min(matchedConstraints, 10.0);
        }

        //calculate image quality score
        ImageQuality imageQuality = image.getImageQuality();
        imageQuality.getBlurinessLevelPenalty();
        imageQuality.getOverExposedPenalty();
        imageQuality.getUnderExposedPenalty();


        double qualityScore = IMAGE_QUALITY_FACTOR * (
                scoreProps.getUnderExposedPenalty() * imageQuality.getUnderExposedPenalty() +
                scoreProps.getOverExposedPenalty() * imageQuality.getOverExposedPenalty() +
                scoreProps.getBlurinessLevelPenalty() * imageQuality.getBlurinessLevelPenalty());

        HashMultimap<String, CharacterQuality> charQualities = image.getCharQualities();
        double charQualityScore = 0;
        int charQualityCount = 0;
        for (String charName : charQualities.keySet()) {
            if (image.getRelevantCharacters().contains(charName)) {
                CharacterQuality charQuality = charQualities.get(charName).iterator().next();
                charQualityScore += getCharQualityScore(image, charQuality);
                charQualityCount++;
            }
        }
        if (charQualityCount > 0) {
            charQualityScore = CHAR_QUALITY_FACTOR * (charQualityScore / (double) charQualityCount);
        }
        double crowdednessScore = CROWD_FACTOR * image.getCrowdedness(); //precalculated in earlier stage
        double result = crowdednessScore + qualityScore + softConstraintsScore + charQualityScore;
        getImagetFitScoreCache.put(pair, result);
        return result;
    }

    private double getCharQualityScore(ImageInstance image, CharacterQuality characterQuality) {
        double facingScore;
        switch (characterQuality.getFacing()) {
            case FRONT:
                facingScore = 1;
                break;
            case UP:
            case DOWN:
            case BOTTOM:
            case SIDE:
                facingScore = 0.25;
                break;
            case BACK:
                facingScore = 0.05;
                break;
            default:
                throw new RuntimeException("Facing " + characterQuality.getFacing() + " not supported");
        }

        ImageQuality imageQuality = image.getImageQuality();
        int imageSize = imageQuality.getHeight() * imageQuality.getWidth();
        Rectangle box = characterQuality.getBox();
        int charSize = box.getHeight() * box.getWidth();

        double sizeScore = (double) charSize / (double) imageSize;
        return sizeScore * facingScore;
    }


    /**
     * Calculate the relative score of an event, in order to greedy process it
     *
     * @param ctx
     * @param event
     * @param nonFuzziness - a number between 0 to 1 that how deterministic the result is.
     *                     nonFuzziness 1: totally deterministic. nonFuzziness 0: very random
     * @return
     */
    public double getEventScore(PipelineContext ctx, StoryEvent event, double nonFuzziness) {
        double result=0;
        if (getEventScoreCache.containsKey(event)){
            result=getEventScoreCache.get(event);
        } else {
            double largestOptions = ctx.getEventToPossibleImages().values()
                    .stream()
                    .mapToInt(Set::size)
                    .max()
                    .getAsInt();
            double largestDegree = ctx.getEventToPossibleImages().keySet()
                    .stream()
                    .mapToInt((event1) -> ctx.getInDependenciesForEvent(event1).size())
                    .max()
                    .getAsInt();
            double degree = ctx.getInDependenciesForEvent(event).size();
            double optionsCount = ctx.getEventToPossibleImages().get(event).size();

            double eventScoreFactor = scoreProps.getEventScoreFactor();
            result = eventScoreFactor * (1.0 - (optionsCount / largestOptions));
            result += (1.0 - eventScoreFactor) * (degree / largestDegree);
            getEventScoreCache.put(event,result);
        }
        result += fuziness(nonFuzziness);
        return result;
    }

    private double fuziness(double nonFuzziness) {
        return RandomUtils.nextDouble(0, 1 - Math.pow(nonFuzziness, 2));
    }
}
