package idc.storyalbum.fetcher;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotoSet;
import com.flickr4java.flickr.photosets.Photoset;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by yonatan on 15/4/2015.
 */
@Controller
@PropertySource("classpath:apiKey.properties")
public class Fechter implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(Fechter.class);

    @Autowired
    private Flickr flickr;

    @Autowired
    private FilterService filterService;

    @Autowired
    private DownloadService downloadService;

    @Override
    public void run(String... strings) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String baseDir = "/tmp/sets";
        System.out.print("Dest dir (" + baseDir + ")? ");

        String uBaseDir = br.readLine();
        if (!StringUtils.isBlank(uBaseDir)) {
            baseDir = uBaseDir;
        }
        System.out.print("Set URL? ");
        String setURL = StringUtils.trim(br.readLine());
        String setId = StringUtils.substringAfterLast(setURL, "/").trim();

        String dir = FilenameUtils.normalize(baseDir + File.separatorChar + setId);
        System.out.print("Any specific tags (enter for none, comma delimited)? ");
        String tagsString = StringUtils.trim(br.readLine());
        Set<String> tags = Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .splitToList(tagsString)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        int page = 1;
        PhotoList<Photo> photos;
        Photoset photoSetInfo = flickr.getPhotosetsInterface().getInfo(setId);
        log.info("Downloading set {} to {}", setId, photoSetInfo.getTitle(), dir);
        if (!tags.isEmpty()) {
            log.info("with filter tags {}", tags);
        }
//https://www.flickr.com/photos/martinrp/sets/72157603658654812
        Set<Photo> photosToDownload = new HashSet<>();
        do {
            log.info("Fetching page {} list", page);
            photos = flickr.getPhotosetsInterface().getPhotos(setId, 500, page);
            log.info("fetched page {} list: {}/{} images",photos.getPage(),photos.size(),photos.getTotal());
            page++;
            for (Photo photo : photos) {
                photosToDownload.add(photo);
            }
        } while (photos.getPages() == page);

//        if (!tags.isEmpty()) {
            Set<Photo> filtered=filterService.filter(photosToDownload, tags);
            log.info("After filtering we have {} photos", filtered.size());
//        }

        writeMeta(dir, setURL, photoSetInfo, tags, filtered);
        downloadService.download(dir, filtered);
        log.info("Downloaded {} files into {}", photosToDownload.size(), dir);
        log.info("Done!");
    }

    private void writeMeta(String dirName, String setURL, Photoset photoSetInfo, Set<String> tags, Set<Photo> photosToDownload) throws IOException {
        Set<String> photoUrls = photosToDownload.stream().map((photo) -> {
            try {
                return photo.getOriginalUrl();
            } catch (FlickrException e) {
                return "N/A";
            }
        }).collect(Collectors.toSet());

        File dir = new File(dirName);
        dir.mkdirs();
        File metaFile = new File(dir, "set.txt");

        FileUtils.writeStringToFile(metaFile, "Taken at " + new Date() + "\n", false);
        FileUtils.writeStringToFile(metaFile, "From " + setURL + "\n", true);
        FileUtils.writeStringToFile(metaFile, "Title: " + photoSetInfo.getTitle() + "\n", true);
        if (tags != null && !tags.isEmpty()) {
            FileUtils.writeStringToFile(metaFile, "Filter by tags: " + tags + "\n", true);
        }
        FileUtils.writeStringToFile(metaFile, "Total of " + photosToDownload.size() + " Files:\n", true);
        FileUtils.writeLines(metaFile, photoUrls, true);
    }
}
