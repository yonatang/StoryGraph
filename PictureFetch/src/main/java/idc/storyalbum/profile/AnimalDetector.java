package idc.storyalbum.profile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by yonatan on 2/5/2015.
 */
public class AnimalDetector {
    private static String urlTemplate = "http://wordnetweb.princeton.edu/perl/webwn?c=6&sub=Change&o2=&o0=&o8=1&o1=&o7=&o5=&o9=&o6=1&o3=&o4=1&i=-1&h=0&s=";

    public boolean isAnimal(String thing) throws Exception {
        URL url = new URL(urlTemplate + URLEncoder.encode(thing, "UTF-8"));
        InputStream is = (InputStream) url.getContent();
        String content = IOUtils.toString(is);
        return StringUtils.containsIgnoreCase(content, "noun.animal");
    }


    public static void main(String... args) throws Exception {
        File allItems = new File("/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Zoo/all-items.txt");
        final AnimalDetector animalDetector = new AnimalDetector();
        final Set<String> animals = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<String> items = FileUtils.readLines(allItems);
        List<Future> futures = new ArrayList<>();
        for (String item : items) {
            Future<?> submit = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String mainItem = StringUtils.substringBefore(item," : ");
                        if (animalDetector.isAnimal(mainItem)) {
                            System.out.println(mainItem + " is an animal!");
                            animals.add(item);
                        } else {
                            System.out.println("~" + mainItem + " is not an animal");
                        }
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
        System.out.println("Animals:");
        SortedSet<String> animalsSorted=new TreeSet<>(animals);
        for (String animal : animalsSorted) {
            System.out.println(animal);
        }
        System.out.println(animals.size());
    }
}
