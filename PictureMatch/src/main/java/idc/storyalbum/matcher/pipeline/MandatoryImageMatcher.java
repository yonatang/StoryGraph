package idc.storyalbum.matcher.pipeline;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import idc.storyalbum.matcher.Consts.Constraints;
import idc.storyalbum.matcher.exception.NoMatchException;
import idc.storyalbum.model.graph.Constraint;
import idc.storyalbum.model.graph.StoryEvent;
import idc.storyalbum.model.image.AnnotatedImage;
import idc.storyalbum.model.image.ImageInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Created by yonatan on 18/4/2015.
 */
@Service
@Slf4j
public class MandatoryImageMatcher {

    @Autowired
    private ScoreService scoreService;

    private boolean possibleMatch(StoryEvent event, AnnotatedImage image) {
        for (Constraint constraint : event.getConstraints()) {
            if (!constraint.isSoft()) {
                if (!ConstraintUtils.isMatch(constraint, image)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void populateImageInstances(PipelineContext ctx, StoryEvent storyEvent, AnnotatedImage annotatedImage) {

        log.debug("  Images chars {}", annotatedImage.getCharacterIds());
        List<Set<Set<String>>> constraintsOptions = new ArrayList<>();
        for (Constraint constraint : storyEvent.getConstraints()) {
            if (!Constraints.TYPE_WHO.equals(constraint.getType()) ||
                    Constraints.OP_EXCLUDE_ALL.equals(constraint.getOperator())) {
                continue;
            }
            log.debug("    Constraint{} {} {}", (constraint.isSoft() ? " soft" : ""), constraint.getOperator(), constraint.getValues());
            // important! add an option to the options only after options will no longer be modified
            // as modifying a set within set is evil
            Set<Set<String>> options = new HashSet<>();
            constraintsOptions.add(options);
            if (Constraints.OP_INCLUDE_ALL.equals(constraint.getOperator())) {
                options.add(new HashSet<>(constraint.getValues()));
            } else {
                List<String> chars = new ArrayList<>(constraint.getValues());
                Iterator<int[]> it = CombinatoricsUtils.combinationsIterator(constraint.getValues().size(), constraint.getExtraN());
                // go through all potential match options
                Set<String> imageChars = annotatedImage.getCharacterIds().elementSet();
                while (it.hasNext()) {
                    int[] pointers = it.next();
                    Set<String> option = new HashSet<>();
                    for (int pointer : pointers) {
                        option.add(chars.get(pointer));
                    }
                    //Check if the option actually match the image, or partially of soft constraint
                    Sets.SetView<String> intersect = Sets.intersection(option, imageChars);
                    if (constraint.isSoft() || intersect.size() == option.size()) {
                        options.add(intersect.immutableCopy());
                    }
                }
            }
        }

        Set<List<Set<String>>> product = Sets.cartesianProduct(constraintsOptions);
        log.debug("    Instances: {}", product.size());
        for (List<Set<String>> instance : product) {
            ImageInstance imageInstance = new ImageInstance(annotatedImage, instance);
            double imageCrowdedness = scoreService.getImageCrowdedness(imageInstance);
            imageInstance.setCrowdedness(imageCrowdedness);
            log.debug("      Instance: {}", imageInstance.getCharacterIds());
            log.debug("      Crowdedness: {}", imageCrowdedness);
            ctx.addPossibleMatch(storyEvent, imageInstance);
        }

    }
    private void findAllPossibleMatches(PipelineContext context) {
        for (StoryEvent storyEvent : context.getEventIdMap().values()) {
            log.debug("{}", storyEvent);
            int imageCountrer = 0;
            for (AnnotatedImage annotatedImage : context.getImageNameMap().values()) {
                if (possibleMatch(storyEvent, annotatedImage)) {
                    imageCountrer++;
                    log.debug("  Potential match: {}", annotatedImage.getImageFilename());
                    populateImageInstances(context, storyEvent, annotatedImage);
                }
            }
            if (log.isDebugEnabled()) {
                if (context.getPossibleMatches(storyEvent).isEmpty()) {
                    log.debug("  No potential matches!");
                    //actually, the NoMatchException can be thrown here
                    //but for the sake of logging more information, i let it
                    //continue and fail the the next phase
                } else {
                    log.debug("  Total annotated images matched: {}", imageCountrer);
                    log.debug("  Total instances matched: {}", context.getPossibleMatches(storyEvent).size());
                }

            }
        }
    }

    /**
     * iterate through all options and try to find if there is an event with single image matching.
     * If so, match it and remove it from all other events. Repeat until stabilize.
     *
     * @param context
     * @throws NoMatchException
     */
    private void filterMandatoryMatches(PipelineContext context) throws NoMatchException {
        final MutableBoolean stable = new MutableBoolean();
        Map<StoryEvent, Set<ImageInstance>> possibleImageMap = context.getEventToPossibleImages();
        do {
            stable.setTrue();

            for (Map.Entry<StoryEvent, Set<ImageInstance>> storyEventSetEntry : possibleImageMap.entrySet()) {
                StoryEvent event = storyEventSetEntry.getKey();
                Set<String> filenames = storyEventSetEntry.getValue().stream()
                        .map(ImageInstance::getImageFilename).collect(toSet());
                if (filenames.size() == 1) {
                    //remove the mandatory match from all other events
                    String theFilename = Iterables.getOnlyElement(filenames);
                    log.debug("  Found a trivial match for {}: {}", event, theFilename);

                    //remove it
                    Set<StoryEvent> possibleEvents = new HashSet<>(context.getImagesToPossibleEvents().get(theFilename));
                    possibleEvents.stream()
                            .filter(possibleStoryEvent -> !possibleStoryEvent.equals(event))
                            .forEach(possibleStoryEvent -> {
                                boolean removed = context.removePossibleMatch(possibleStoryEvent, theFilename);
                                if (removed) {
                                    stable.setFalse();
                                }
                            });
                }
                if (filenames.size() == 0) {
                    throw new NoMatchException("Couldn't find match for event " + event.getId() + ":" + event.getName());
                }
            }
        } while (stable.isFalse());
        if (log.isDebugEnabled()) {
            log.debug("Filtered potential matches:");
            List<StoryEvent> events = new ArrayList<>(possibleImageMap.keySet());
            events.sort((o1, o2) -> Integer.compare(o1.getId(),o2.getId()));
            for (StoryEvent event : events) {
                Set<ImageInstance> possibleImages = possibleImageMap.get(event);
                log.debug("  {}", event);
                for (ImageInstance possibleImage : possibleImages) {
                    log.debug("    Potential match: {}", possibleImage.getImageFilename());
                }
            }
        }
    }

    public void match(PipelineContext context) throws NoMatchException {
        log.info("Finding all potential matches");
        findAllPossibleMatches(context);
        //calculate average images per node
        int i = 0;
        Map<StoryEvent, Set<ImageInstance>> eventToPossibleImages = context.getEventToPossibleImages();
        for (Set<ImageInstance> storyEvents : eventToPossibleImages.values()) {
            i += storyEvents.size();
        }
        log.info("Average number of pictures per node: {}", i / (double) eventToPossibleImages.size());
        log.info("Fixing trivial matches");
        filterMandatoryMatches(context);
        log.info("All potential images are set");

    }
}
