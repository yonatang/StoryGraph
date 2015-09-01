package idc.storyalbum.layout;

import com.fasterxml.jackson.databind.ObjectMapper;
import idc.storyalbum.layout.model.layout.Layout;
import idc.storyalbum.layout.model.template.PageTemplate;
import idc.storyalbum.layout.service.RenderResult;
import idc.storyalbum.layout.service.TemplateReader;
import idc.storyalbum.layout.service.searcher.ExhaustiveSearcher;
import idc.storyalbum.model.album.Album;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Set;

/**
 * Created by yonatan on 14/5/2015.
 */
@Component
@Slf4j
public class Runner implements CommandLineRunner {
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${story-album.album-file}")
    private String albumFile;

    @Value("${stoyr-album.max-pages:0}")
    private int maxPageLayouts;

    @Value("${story-album.output-dir}")
    private String outputDir;

    @Autowired
    private TemplateReader templateReader;

    @Autowired
    private ExhaustiveSearcher searcher;

    @Autowired
    private RenderResult renderResult;
    @Override
    public void run(String... args) throws Exception {
        File file = new File(albumFile);
        log.info("Loading album file {}", file);
        Album album = objectMapper.readValue(file, Album.class);
        Set<PageTemplate> templates = templateReader.readTemplates();
        log.info("Starting to search for layouts");
        Layout bestLayout = searcher.searchLayout(album, templates, maxPageLayouts);
        log.info("Found a layout of {} pages with score {}", bestLayout.getPages().size(), bestLayout.getScore());

        renderResult.renderLayout(bestLayout,new File(outputDir));
    }
}
