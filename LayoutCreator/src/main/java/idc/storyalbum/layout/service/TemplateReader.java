package idc.storyalbum.layout.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import idc.storyalbum.layout.model.template.PageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yonatan on 15/5/2015.
 */
@Service
@Slf4j
public class TemplateReader {
    @Value("${story-album.template-dir}")
    private String templateDirName;

    @Autowired
    private ObjectMapper objectMapper;

    public Set<PageTemplate> readTemplates() {
        File templateDir = new File(templateDirName);
        log.info("Reading template dir {}", templateDir);

        Collection<File> files = FileUtils.listFiles(templateDir, new String[]{"json"}, false);
        Set<PageTemplate> templates = new HashSet<>();
        for (File file : files) {
            try {
                PageTemplate pageTemplate = objectMapper.readValue(file, PageTemplate.class);
                templates.add(pageTemplate);
            } catch (Exception e) {
                log.warn("Error while parsing template {} - {}", file, e);
            }
        }
        log.info("Found {} templates", templates.size());
        return templates;
    }

}
