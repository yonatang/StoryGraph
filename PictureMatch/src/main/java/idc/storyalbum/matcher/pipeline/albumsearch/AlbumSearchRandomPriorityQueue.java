package idc.storyalbum.matcher.pipeline.albumsearch;

import idc.storyalbum.matcher.conf.Props;
import idc.storyalbum.matcher.pipeline.PipelineContext;
import idc.storyalbum.matcher.pipeline.ScoreService;
import idc.storyalbum.model.album.Album;
import idc.storyalbum.model.album.AlbumPage;
import idc.storyalbum.model.graph.StoryEvent;
import idc.storyalbum.model.graph.StoryGraph;
import idc.storyalbum.model.image.ImageInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.stream.Collectors.toList;

/**
 * Created by yonatan on 19/4/2015.
 * Heuristically searches for the best matching albums it found according to
 * a partially random search
 */
@Service("priorityQueue")
@Slf4j
public class AlbumSearchRandomPriorityQueue extends AlbumSearch {

    @Autowired
    Props.SearchPriorityProps searchPriorityProps;

    @Value("${story-album.search.num-of-results}")
    int NUM_OF_BEST_RESULTS;


    private class ImageMatchPriorityQueue extends PriorityQueue<ImageInstance> {
        public ImageMatchPriorityQueue(ScoreService scoreService, StoryEvent event, double nonFuzziness) {
            super((o1, o2) -> {
                double o1Score = scoreService.getImageFitScore(o1, event,nonFuzziness);
                double o2Score = scoreService.getImageFitScore(o2, event,nonFuzziness);
                //sort largest first
                return Double.compare(o2Score, o1Score);
            });
        }

        public void removeByFilename(String imageFilename) {
            List<ImageInstance> toRemove = stream()
                    .filter(x -> StringUtils.equals(imageFilename, x.getImageFilename()))
                    .collect(toList());
            removeAll(toRemove);
        }
    }

    class EventPriorityQueue extends PriorityQueue<StoryEvent> {
        public EventPriorityQueue(PipelineContext ctx, ScoreService scoreService, double nonFuzziness) {
            super((o1, o2) -> {
                double o1Scope = scoreService.getEventScore(ctx, o1, nonFuzziness);
                double o2Scope = scoreService.getEventScore(ctx, o2, nonFuzziness);
                //sort largest first
                return Double.compare(o2Scope, o1Scope);
            });
        }
    }

    Set<AlbumPage> findAssignment(PipelineContext ctx, int t) {
        int M=searchPriorityProps.getNumOfRepetitions();
        StoryGraph storyGraph = ctx.getStoryGraph();
        double nonFuzziness = (double) t / (double) M;
        EventPriorityQueue eventQueue = new EventPriorityQueue(ctx, scoreService, nonFuzziness);
        Map<StoryEvent, ImageMatchPriorityQueue> queues = new HashMap<>();
        for (StoryEvent storyEvent : storyGraph.getEvents()) {
            ImageMatchPriorityQueue queue = new ImageMatchPriorityQueue(scoreService, storyEvent, nonFuzziness);
            queues.put(storyEvent, queue);
            Set<ImageInstance> possibleMatches = ctx.getPossibleMatches(storyEvent);
            log.trace("Adding for {}:{} possible images {}", storyEvent.getId(), storyEvent.getName(), possibleMatches);
            queue.addAll(possibleMatches);
            eventQueue.add(storyEvent);
        }

        Set<AlbumPage> assignment = new HashSet<>();
        while (!eventQueue.isEmpty()) {
            StoryEvent event = eventQueue.poll();
            ImageMatchPriorityQueue images = queues.get(event);
            if (images.isEmpty()) {
                //break and continue to next solution
                return null;
            }
            ImageInstance bestImage = images.poll();

            for (ImageMatchPriorityQueue queue : queues.values()) {
                queue.removeByFilename(bestImage.getImageFilename());
            }
            assignment.add(new AlbumPage(bestImage, event));
        }
        return assignment;
    }

    public SortedSet<Album> findAlbumsImpl(PipelineContext ctx) {
        int M=searchPriorityProps.getNumOfRepetitions();
        log.info("Searching for {} best albums, Priority Queue strategy, using {} iterations", NUM_OF_BEST_RESULTS, M);
        SortedSet<Album> bestAlbums =
                //sort largest first
                new TreeSet<>((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));

        StopWatch s3 = new StopWatch();
        s3.start();
        long split = System.currentTimeMillis();
        DecimalFormat df1 = new DecimalFormat("#.##");
        DecimalFormat df2 = new DecimalFormat("#.####");
        for (int i = 0; i < M; i++) {
            if (System.currentTimeMillis() - split > 60000) {
                double percentage = (((double) i / (double) M) * 100.0);
                Album f = bestAlbums.first();
                double bestScore = f.getScore();
                log.info("{}% completed, best for now {}", df1.format(percentage), df2.format(bestScore));
                split = System.currentTimeMillis();
            }
            Set<AlbumPage> assignment = findAssignment(ctx, i);
            if (assignment == null) {
                continue;
            }
            double score = evaluateFitness(ctx, assignment);
            Album album = new Album();
            album.setPages(sortPages(assignment));
            album.setScore(score);
            album.setBaseDir(ctx.getAnnotatedSet().getBaseDir());
            album.setIterations(M);
            bestAlbums.add(album);
            while (bestAlbums.size() > NUM_OF_BEST_RESULTS) {
                bestAlbums.remove(bestAlbums.last());
            }
        }
        s3.stop();
        log.info("Found {} albums with scores {}", bestAlbums.size(), bestAlbums.stream().map(Album::getScore).collect(toList()));
        log.info("Took {}", s3.toString());
        log.debug("In average, {}ms per iteration", s3.getNanoTime() / M / 1000);
        return bestAlbums;
    }

}
