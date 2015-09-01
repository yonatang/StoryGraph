package idc.storyalbum.profile;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by yonatan on 2/5/2015.
 */
public class AnimalBookCreator {

    public static void main(String... args) throws Exception {
        Flickr flickr = new Flickr("??", "??", new REST());
        File animalsFile = new File("/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Zoo/all-animals.txt");

        final Map<String, String> animalsPhotos = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<String> animals = FileUtils.readLines(animalsFile);
        List<Future> futures=new ArrayList<>();

        for (String animal : animals) {
            Future<?> submit = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String q = StringUtils.substringBefore(animal, " : ");
                        SearchParameters searchParameters = new SearchParameters();
                        searchParameters.setText(q);
                        PhotoList<Photo> photos = null;
                        photos = flickr.getPhotosInterface().search(searchParameters, 1, 1);
                        String image = photos.get(0).getSmallUrl();
                        animalsPhotos.put(q, image);
                        System.out.println(q + " " + image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            futures.add(submit);
        }
        for (Future future : futures) {
            future.get();
        }

        System.out.println();
        System.out.println();
        System.out.println("<html><body>");
        for (String animal : animals) {
            String q = StringUtils.substringBefore(animal, " : ");
            System.out.println("<h1>"+q+"</h1>");
            System.out.println("<img src='"+animalsPhotos.get(q)+"'>");
            System.out.println("<br/><br/><br/>");
        }
        System.out.println("</body></html>");
    }

}