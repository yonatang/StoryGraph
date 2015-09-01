package idc.storyalbum.profile;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

/**
 * Created by yonatan on 2/5/2015.
 */
public class AllItems {
    public static void main(String... args) throws Exception {
        String base = "/Users/yonatan/Dropbox/Studies/Story Albums/Sets/Zoo";

        List<String> setFolders = Lists.newArrayList("72157600312588222",
                "72157603658654812",
                "72157604991613315",
                "72157608170963283",
                "72157629767911319",
                "72157649437112944",
                "72157649504878953",
                "72157650229767958",
                "72157650526307885");

        SortedMap<String, String> allItems=new TreeMap<>();
//        SortedSet<String> allItems=new TreeSet<>();
        for (String setFolder : setFolders) {
            File dir=new File(base+"/"+setFolder+"/items");
            Collection<File> files = FileUtils.listFiles(dir, new String[]{"txt"}, false);
            for (File file : files) {
                if (file.getName().endsWith("set.txt")){
                    continue;
                }
                List<String> lines = FileUtils.readLines(file);
                for (String line : lines) {
                    String confidentLevelStr = StringUtils.substringAfterLast(line, ":");
                    String items=StringUtils.substringBeforeLast(line, ":");
                    double confLvl=Double.parseDouble(confidentLevelStr);
                    if (confLvl>0.3){
                        Iterable<String> split = Splitter.on(',').trimResults().omitEmptyStrings().split(items);
                        SortedSet<String> foundItems = new TreeSet<>();
                        for (String item : split) {
                            foundItems.add(item.toLowerCase());
                        }
                        for (String foundItem : foundItems) {
                            allItems.put(foundItem,foundItems.toString());
                        }
                    }
                }
            }
        }
        for (String item : allItems.keySet()) {
            System.out.println(item+ " : "+allItems.get(item));
        }
        System.out.println(allItems.size());
    }
}
