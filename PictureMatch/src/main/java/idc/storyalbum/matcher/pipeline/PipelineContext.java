package idc.storyalbum.matcher.pipeline;

import idc.storyalbum.model.graph.StoryDependency;
import idc.storyalbum.model.graph.StoryEvent;
import idc.storyalbum.model.graph.StoryGraph;
import idc.storyalbum.model.image.AnnotatedImage;
import idc.storyalbum.model.image.AnnotatedSet;
import idc.storyalbum.model.image.ImageInstance;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Created by yonatan on 18/4/2015.
 */
@Data
public class PipelineContext {
    public PipelineContext(StoryGraph storyGraph, AnnotatedSet set) {
        this.annotatedSet = set;
        this.storyGraph = storyGraph;

        this.eventIdMap = storyGraph.getEvents()
                .stream()
                .collect(toMap(StoryEvent::getId, identity()));
        this.eventToPossibleImages = storyGraph.getEvents().stream()
                .collect(toMap(identity(), x -> new HashSet<>()));

        this.imageNameMap = annotatedSet.getImages()
                .stream()
                .collect(toMap(annotatedImage -> annotatedImage.getImageFilename(), identity()));
        this.imagesToPossibleEvents = set.getImages().stream()
                .collect(toMap(AnnotatedImage::getImageFilename, x -> new HashSet<>()));
    }

    private AnnotatedSet annotatedSet;
    private StoryGraph storyGraph;

    private Set<ImageInstance> imageInstances=new HashSet<>();
    private Map<Integer, StoryEvent> eventIdMap = new HashMap<>();
    private Map<String, AnnotatedImage> imageNameMap = new HashMap<>();
    private Set<String> assignedImages = new HashSet<>();
    private Set<Integer> assignedEvents = new HashSet<>();
    private Map<StoryEvent, Set<ImageInstance>> eventToPossibleImages;
    private Map<String, Set<StoryEvent>> imagesToPossibleEvents;

    /**
     * Add to an event a possible image
     *
     * @param event
     * @param image
     */
    public void addPossibleMatch(StoryEvent event, ImageInstance image) {
        if (!eventToPossibleImages.containsKey(event)) {
            eventToPossibleImages.put(event, new HashSet<>());
        }
        eventToPossibleImages.get(event).add(image);
        String filename=image.getImageFilename();
        if (!imagesToPossibleEvents.containsKey(filename)) {
            imagesToPossibleEvents.put(filename, new HashSet<>());
        }
        imagesToPossibleEvents.get(filename).add(event);
    }

    /**
     * Remove from an event a possible match
     *
     * @param event
     * @param filename
     */
    public boolean removePossibleMatch(StoryEvent event, String filename) {

        Set<ImageInstance> imageInstances = eventToPossibleImages.get(event);
        Set<ImageInstance> imagesToRemove = imageInstances.stream()
                .filter(instance -> instance.getImageFilename().equals(filename)).collect(toSet());

        boolean removed = imageInstances.removeAll(imagesToRemove);
        imagesToPossibleEvents.get(filename).remove(event);
        return removed;
    }

    public Set<ImageInstance> getPossibleMatches(StoryEvent event) {
        return eventToPossibleImages.get(event);
    }

    public Set<StoryDependency> getInDependenciesForEvent(StoryEvent event) {
        return storyGraph.getDependencies().stream()
                .filter((dep) -> dep.getToEventId() == event.getId())
                .collect(Collectors.toSet());
    }
}
