package idc.storyalbum.matcher.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Created by yonatan on 26/12/2015.
 */
@Configuration
@EnableConfigurationProperties
public class Props {

    @Configuration
    @ConfigurationProperties(prefix = "story-album.file")
    @Data
    public static class FileProps {
        private String annotatedSetPath;
        private String storyPath;
        private String outputPath;
    }

    @Component
    @ConfigurationProperties(prefix = "story-album.search")
    @Data
    public static class SearchProps {
        //story-album.search.strategyName
        //story-album.search.strategy-name
        private String strategyName;
    }

    @Configuration
    @ConfigurationProperties(prefix = "story-album.scores")
    @Data
    public static class ScoreProps {
        private double qualityFactor;
        private double underExposedPenalty;
        private double blurinessLevelPenalty;
        private double overExposedPenalty;
        private double eventScoreFactor = 0.5;
        private boolean skinDepCalc = false;
    }

    @Configuration
    @ConfigurationProperties(prefix = "story-album")
    @Data
    public static class GlobalProps {
        private boolean debugAlbum;
        private String debugAlbumFullPath;

    }

    @Configuration
    @ConfigurationProperties(prefix = "story-album.search.priority")
    @Data
    public static class SearchPriorityProps {
        private int numOfRepetitions;
    }


}
