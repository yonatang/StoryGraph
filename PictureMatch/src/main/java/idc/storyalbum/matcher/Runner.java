package idc.storyalbum.matcher;

import idc.storyalbum.matcher.conf.Props;
import idc.storyalbum.matcher.exception.NoMatchException;
import idc.storyalbum.matcher.exception.TemplateErrorException;
import idc.storyalbum.matcher.pipeline.DataIOService;
import idc.storyalbum.matcher.pipeline.MandatoryImageMatcher;
import idc.storyalbum.matcher.pipeline.PipelineContext;
import idc.storyalbum.matcher.pipeline.StoryTextResolver;
import idc.storyalbum.matcher.pipeline.albumsearch.AlbumSearch;
import idc.storyalbum.matcher.pipeline.albumsearch.AlbumSearchFactory;
import idc.storyalbum.matcher.tools.html_album.ConvertToHtml;
import idc.storyalbum.model.album.Album;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.SortedSet;

/**
 * Created by yonatan on 18/4/2015.
 */
@Component
@Slf4j
public class Runner implements CommandLineRunner {

    @Autowired
    private AlbumSearchFactory albumSearchFactory;

    @Autowired
    private Props.GlobalProps globalProps;

    @Autowired
    private Props.SearchProps searchProps;

    @Autowired
    private Props.FileProps fileProps;

    private AlbumSearch albumSearch;

    @PostConstruct
    void init() {
        log.debug("Loading strategy {}", searchProps.getStrategyName());
        albumSearch = albumSearchFactory.getAlbumSearch(searchProps.getStrategyName());
    }

    @Autowired
    private DataIOService dataIOService;

    @Autowired
    private MandatoryImageMatcher mandatoryImageMatcher;

    @Autowired
    private StoryTextResolver storyTextResolver;

    @Override
    public void run(String... args) throws Exception {
        try {
            File annotatedSetFile = new File(fileProps.getAnnotatedSetPath()); //"/Users/yonatan/StoryAlbumData/Riddle/Set1/annotatedSet.json"); //new File("/tmp/annotatedSet.json");
            File storyGraphFile = new File(fileProps.getStoryPath()); //"/Users/yonatan/StoryAlbumData/Riddle/story.json");
            PipelineContext ctx = dataIOService.readData(storyGraphFile, annotatedSetFile);
            mandatoryImageMatcher.match(ctx);
            SortedSet<Album> bestAlbums = albumSearch.findAlbums(ctx);
            Album bestAlbum = bestAlbums.first();
            storyTextResolver.resolveText(bestAlbum, ctx.getStoryGraph().getProfile());

            String albumPath = FilenameUtils.getFullPath(fileProps.getAnnotatedSetPath());
            String outputPath = fileProps.getOutputPath();

            if (StringUtils.isBlank(outputPath)) {
                outputPath = albumPath;
            }
            new File(outputPath).mkdirs();
            File albumFile = new File(outputPath + File.separatorChar + "album.json");

            dataIOService.writeAlbum(bestAlbum, albumFile);
            if (globalProps.isDebugAlbum()) {
                File debugHtmlFile = new File(outputPath, "album-" + searchProps.getStrategyName() + ".html");
                log.info("Producing a debug album {}", debugHtmlFile);
                ConvertToHtml.write(albumFile, debugHtmlFile, globalProps.getDebugAlbumFullPath());
            }
        } catch (NoMatchException e) {
            log.error("Error! Cannot satisfy story constraints: {}", e.getMessage());
        } catch (TemplateErrorException e) {
            log.error("Error! Cannot process template: {}", e.getMessage());
        }
    }
}
