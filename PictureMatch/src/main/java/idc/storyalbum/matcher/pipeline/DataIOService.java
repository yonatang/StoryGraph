package idc.storyalbum.matcher.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import idc.storyalbum.matcher.conf.Props;
import idc.storyalbum.model.album.Album;
import idc.storyalbum.model.album.AlbumPage;
import idc.storyalbum.model.graph.Constraint;
import idc.storyalbum.model.graph.StoryDependency;
import idc.storyalbum.model.graph.StoryEvent;
import idc.storyalbum.model.graph.StoryGraph;
import idc.storyalbum.model.image.AnnotatedImage;
import idc.storyalbum.model.image.AnnotatedSet;
import idc.storyalbum.model.image.ImageQuality;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yonatan on 18/4/2015.
 */
@Service
@Slf4j
public class DataIOService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Props.GlobalProps globalProps;

    public PipelineContext readData(File graphFile, File setFile) throws IOException {
        return new PipelineContext(readStoryGraph(graphFile), readAnnotatedSet(setFile));
    }

    private StoryGraph readStoryGraph(File file) throws IOException {
        log.info("Reading file {}", file);
        StoryGraph storyGraph = objectMapper.readValue(file, StoryGraph.class);
        log.info("Read graph with {} nodes and {} edges", storyGraph.getEvents().size(), storyGraph.getDependencies().size());
        if (log.isDebugEnabled()) {
            List<StoryEvent> sortedEvents = new ArrayList<>(storyGraph.getEvents());
            sortedEvents.sort((o1, o2) -> o1.getId() - o2.getId());
            for (StoryEvent storyEvent : sortedEvents) {
                log.debug("  {}", storyEvent);
                log.debug("  Text: \"{}\"", StringEscapeUtils.escapeJava(storyEvent.getText()));
                for (Constraint constraint : storyEvent.getConstraints()) {
                    log.debug("    {}", constraint);
                }
            }
            for (StoryDependency storyDependency : storyGraph.getDependencies()) {
                log.debug("  {}", storyDependency);
            }
        }
        return storyGraph;
    }

    private AnnotatedSet readAnnotatedSet(File file) throws IOException {
        log.info("Reading set data {}", file);
        AnnotatedSet annotatedSet = objectMapper.readValue(file, AnnotatedSet.class);
        File base = new File(globalProps.getDebugAlbumFullPath(), annotatedSet.getBaseDir().getAbsolutePath());
        for (AnnotatedImage annotatedImage : annotatedSet.getImages()) {
            File imageFile = new File(base, annotatedImage.getImageFilename());
            log.debug("Reading content of file {}", imageFile);
            BufferedImage read = ImageIO.read(imageFile);
            ImageQuality imageQuality = annotatedImage.getImageQuality();
            imageQuality.setWidth(read.getWidth());
            imageQuality.setHeight(read.getHeight());
        }
        log.info("Read set with {} images", annotatedSet.getImages().size());
        return annotatedSet;
    }

    public void writeAlbum(Album album, File file) throws IOException {
        album.setDate(new Date());
        log.info("Writing down an album with score {} to {}", album.getScore(), file);
        if (log.isDebugEnabled()) {
            for (AlbumPage albumPage : album.getPages()) {
                StoryEvent storyEvent = albumPage.getStoryEvent();
                AnnotatedImage image = albumPage.getImage();
                log.debug("  {} matched to {}", storyEvent, image.getImageFilename());
                for (Constraint constraint : storyEvent.getConstraints()) {
                    log.debug("    {}", constraint);
                }
                log.debug("    Location: {}", image.getLocationId());
                log.debug("    Characters: {}", image.getCharacterIds());
                log.debug("    Items: {}", image.getItemIds());
                log.debug("    Date: {}", image.getImageDate());
                log.debug("    Quality: {}", image.getImageQuality());
            }
        }
        objectMapper.writeValue(file, album);
        log.info("File {} was written. File size {}", file, file.length());

    }

}
