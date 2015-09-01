package idc.storyalbum.fetcher;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by yonatan on 16/4/2015.
 */
@Service
public class DownloadService {
    Logger log = LoggerFactory.getLogger(DownloadService.class);

    @Autowired
    private RestTemplate restTemplate;

    public void download(String dir, Set<Photo> photos) throws FlickrException, InterruptedException {
        log.info("Starting to download {} photos", photos.size());
        int counter = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (Photo photo : photos) {
            String prefix="";
            try {
                if (StringUtils.containsIgnoreCase(photo.getLocality().getName(), "machu") ||
                        StringUtils.containsIgnoreCase(photo.getLocality().getName(), "macchu")) {
                    prefix = "mp_";
                } else if (StringUtils.containsIgnoreCase(photo.getRegion().getName(), "cusco")) {
                    prefix = "cusco_";
                }
            } catch (Exception e){}



            String url = StringUtils.defaultString(photo.getOriginalUrl());
            String ext = StringUtils.lowerCase(StringUtils.substringAfterLast(url, "."));
            switch (ext) {
                case "jpg":
                case "jpeg":
                case "gif":
                case "png":
                    executorService.submit(new DownloadPictureTask(photo.getOriginalUrl(), dir,
                            prefix+counter + "." + ext));
                    counter++;
                    break;
                default:
                    log.warn("Skipping file {}, as it is not supported", url);
            }
        }
        executorService.shutdown();
        executorService.awaitTermination(8, TimeUnit.HOURS);
    }

    public class DownloadPictureTask implements Callable<Void> {
        private final String toFileName;
        private final String toDir;

        private final String url;

        public DownloadPictureTask(String url, String dir, String toFileName) {
            this.url = url;
            this.toDir = dir;
            this.toFileName = toFileName;
        }

        @Override
        public Void call() throws Exception {
            File file = new File(FilenameUtils.concat(toDir, toFileName));
            log.debug("Downloading {} as {}", url, file);
            try {
                byte[] forObject = restTemplate.getForObject(url, byte[].class);
                FileUtils.writeByteArrayToFile(file, forObject);
                log.info("{} as saved as {}. File size: {}", url, file, forObject.length);
            } catch (IOException e) {
                log.error("Failed to download {} to {}: {}", url, file, e);
            }
            return null;
        }
    }


}
