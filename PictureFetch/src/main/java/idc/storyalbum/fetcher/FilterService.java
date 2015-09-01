package idc.storyalbum.fetcher;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.tags.Tag;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by yonatan on 16/4/2015.
 */
@Service
public class FilterService {
    Logger log = LoggerFactory.getLogger(DownloadService.class);

    @Autowired
    private Flickr flickr;

    public class FilterTagTask implements Callable<Pair<Photo, Photo>> {
        private final Photo photo;
        private final Set<String> tags;

        public FilterTagTask(Photo photo, Set<String> tags) {
            this.photo = photo;
            this.tags = tags;
        }

        @Override
        public Pair<Photo, Photo> call() throws Exception {
            Photo info = flickr.getPhotosInterface().getInfo(photo.getId(), photo.getSecret());
            Set<String> infoTagSet = info.getTags()
                    .stream()
                    .map(Tag::getValue)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            boolean result = true;
            try {
                String url = photo.getOriginalUrl();
                log.debug("Tags: {} {} for {}", infoTagSet, info.getTags(), url);
                if (Sets.intersection(tags, infoTagSet).size() != tags.size()) {
                    result = false;
                    log.debug("Filtered {} due to mismatching tag", url);
                }
//                System.out.println(info.getLocality().getName());
//                System.out.println(info.getCounty().getName());
//                System.out.println(info.getRegion().getName());
//                System.out.println("---");
                int license = NumberUtils.toInt(info.getLicense(), 0);
                log.debug("License: {} for {}", license, url);
                if (license == 0 || license > 7) {
                    //non CC license
                    result = false;
                    log.info("Filtered {} due to mismatching license {}", url, info.getLicense());
                }
//                if (!StringUtils.containsIgnoreCase(info.getLocality().getName(), "macchu") &&
//                        !StringUtils.containsIgnoreCase(info.getLocality().getName(), "machu") &&
//                        !StringUtils.containsIgnoreCase(info.getRegion().getName(), "cusco")) {
//                    result = false;
//                    log.info("Filtered {} due to mismatching location", url);
//                } else {
//                    log.info("Place: {} {} {}", info.getLocality().getName(), info.getRegion().getName(),info.getCounty().getName());
//                }
            } catch (Exception e) {
                log.warn("Error while reading photo {}", photo.getUrl());
                result = false;
            }
            if (result) {
                return new ImmutablePair<>(photo, info);
            } else {
                return new ImmutablePair<>(photo, null);
            }
//            return new ImmutablePair<>(photo, result);

        }
    }

    public Set<Photo> filter(Set<Photo> photos, Set<String> tags) throws ExecutionException, InterruptedException, FlickrException {
        log.info("Removing from {} photos photos without tags {}", photos.size(), tags);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<Future<Pair<Photo, Photo>>> futures = new ArrayList<>();
        for (Photo photo : photos) {
            futures.add(executorService.submit(new FilterTagTask(photo, tags)));
        }
        executorService.shutdown();
        Set<Photo> result = new HashSet<>();
        for (Future<Pair<Photo, Photo>> future : futures) {
            Pair<Photo, Photo> photoBooleanPair = future.get();
            Photo photo = photoBooleanPair.getLeft();
            String url;
            try {
                url = photo.getOriginalUrl();
            } catch (Exception e) {
                url = photo.getUrl();
            }
            Photo detailedPhoto = photoBooleanPair.getRight();
            if (detailedPhoto == null) {
                log.info("Filtered {}", url);
                photos.remove(photo);
            } else {
                result.add(detailedPhoto);
            }
        }
        return result;

    }

}
