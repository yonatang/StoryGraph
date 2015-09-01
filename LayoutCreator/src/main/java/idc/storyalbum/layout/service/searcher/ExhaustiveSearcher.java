package idc.storyalbum.layout.service.searcher;

import com.google.common.collect.Lists;
import idc.storyalbum.layout.model.layout.Layout;
import idc.storyalbum.layout.model.template.PageTemplate;
import idc.storyalbum.model.album.Album;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Created by yonatan on 16/5/2015.
 */
@Service
@Slf4j
public class ExhaustiveSearcher extends AbsSearcher {

    private void generatePartialList(List<List<Integer>> result,
                                     Integer[] arr, int pointer, int partialSum, Set<Integer> sizes, int target, int sizeLimit) {
        if (partialSum == target) {
            result.add(Lists.newArrayList(ArrayUtils.subarray(arr,0,pointer)));
            return;
        }
        if (partialSum > target || (sizeLimit > 0 && pointer == sizeLimit)) {
            return;
        }
        for (Integer size : sizes) {
            arr[pointer] = size;
            generatePartialList(result, arr, pointer + 1, partialSum + size, sizes, target, sizeLimit);
        }
    }

    /**
     * i.e. for layouts of sizes 1,2,3 and target of 3 will get:
     * [1,1,1], [1,2], [2,1], [3]
     *
     * @param layoutSizes
     * @param target
     * @param sizeLimit   <1 if unlimited.
     * @return
     */
    private List<List<Integer>> allPartitions(Set<Integer> layoutSizes, int target, int sizeLimit) {
        List<List<Integer>> result = new ArrayList<>();
        layoutSizes = layoutSizes.stream().filter(x -> x > 0).collect(toSet());
        if (sizeLimit < 1) {
            sizeLimit = target;
        }
        Integer[] arr = new Integer[sizeLimit];
        generatePartialList(result, arr, 0, 0, layoutSizes, target, sizeLimit);
        return result;
    }

    Layout searchLayoutImpl(Album album, Map<Integer, Set<PageTemplate>> templatesBySize, int maxPageLayouts) {
        Set<Integer> availableSizes = templatesBySize.keySet();
        List<List<Integer>> layoutOptions = allPartitions(availableSizes, album.getPages().size(), maxPageLayouts);
        if (log.isDebugEnabled()) {
            for (List<Integer> layoutOption : layoutOptions) {
                log.debug("  Option: {}", layoutOption);
            }
            log.debug("  Searching in total of {} options", layoutOptions.size());
        }
        double score = Double.NEGATIVE_INFINITY;
        Layout best = null;
        for (List<Integer> layoutOption : layoutOptions) {
            Layout result = findBestMatchingLayout(templatesBySize, album, layoutOption);
            if (result.getScore() > score) {
                best = result;
                score = result.getScore();
            }
        }
        return best;
    }

}
