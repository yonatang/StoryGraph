package idc.storyalbum.layout.model.layout;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yonatan on 14/5/2015.
 */
@Data
public class Layout {
    @Setter(AccessLevel.NONE)
    private List<PageLayout> pages=new ArrayList<>();

    public Double getScore(){
        return pages.stream().mapToDouble(PageLayout::getScore).sum();
    }
}
