package idc.storyalbum.layout.service.searcher;

import idc.storyalbum.layout.model.layout.ImageFrame;
import idc.storyalbum.layout.model.layout.Layout;
import idc.storyalbum.layout.model.layout.PageLayout;
import idc.storyalbum.layout.model.template.Frame;
import idc.storyalbum.layout.model.template.PageTemplate;
import idc.storyalbum.layout.service.ImageService;
import idc.storyalbum.model.album.Album;
import idc.storyalbum.model.album.AlbumPage;
import idc.storyalbum.model.image.Rectangle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Created by yonatan on 16/5/2015.
 */
@Slf4j
public abstract class AbsSearcher {

    @Autowired
    private ImageService imageService;

    abstract Layout searchLayoutImpl(Album album, Map<Integer, Set<PageTemplate>> templatesBySize, int maxPageLayouts);

    public Layout searchLayout(Album album, Set<PageTemplate> templates, int maxPageLayouts) {
        log.info("Searching for album with {} picutres using {} templates", album.getPages().size(), templates.size());
        if (maxPageLayouts > 0) {
            log.info("Limited to {} pages in the output layout", maxPageLayouts);
        }
        Map<Integer, Set<PageTemplate>> templatesBySize
                = templates.stream().collect(groupingBy(x -> x.getFrames().size(), toSet()));
        if (templatesBySize.containsKey(0)) {
            log.warn("Template(s) with zero images found. Not using it.");
            templatesBySize.remove(0);
        }

        Layout best = searchLayoutImpl(album, templatesBySize, maxPageLayouts);
        return best;
    }


    Layout findBestMatchingLayout(Map<Integer, Set<PageTemplate>> templatesBySize,
                                  Album album,
                                  List<Integer> layoutOption) {
        int pageIdx = 0;
        Layout layout = new Layout();
        for (Integer size : layoutOption) {
            PageLayout pageLayout = findBestMatchingPageTemplate(album, templatesBySize, size, pageIdx);
            layout.getPages().add(pageLayout);
            pageIdx += size;
        }
        return layout;
    }

    private PageLayout findBestMatchingPageTemplate(Album album, Map<Integer, Set<PageTemplate>> templatesBySize,
                                                    Integer size, int pageIdx) {
        log.debug("  Searching for pages of size {}", size);
        Set<PageTemplate> pageTemplates = templatesBySize.get(size);
        PageLayout best = null;
        double score = Double.NEGATIVE_INFINITY;
        for (PageTemplate pageTemplate : pageTemplates) {
            PageLayout candidate = createPageLayout(pageTemplate, album, pageIdx);
            if (candidate.getScore() > score) {
                best = candidate;
                score = best.getScore();
            }
        }
        return best;
    }

    private PageLayout createPageLayout(PageTemplate pageTemplate, Album album, int pageIdx) {
        log.debug("  Page Template {}", pageTemplate);
        int templateSize = pageTemplate.getFrames().size();
        List<AlbumPage> subAlbumPages = album.getPages().subList(pageIdx, pageIdx + templateSize);
        PageLayout pageLayout = new PageLayout();
        pageLayout.setHeight(pageTemplate.getHeight());
        pageLayout.setWidth(pageTemplate.getWidth());
        double score = 0;
        for (int i = 0; i < templateSize; i++) {
            AlbumPage albumPage = subAlbumPages.get(i);
            Frame templateFrame = pageTemplate.getFrames().get(i);
            log.debug("    Trying templateFrame {}", templateFrame);
            ImageFrame imageFrame = new ImageFrame();
            pageLayout.getImageFrames().add(imageFrame);
            imageFrame.setImageRect(new Rectangle(templateFrame.getImageRect()));
            imageFrame.setTextRect(new Rectangle(templateFrame.getTextRect()));
            imageFrame.setText(albumPage.getText());


            BufferedImage bufferedImage = imageService.loadImage(album.getBaseDir(), albumPage.getImage().getImageFilename());
            log.debug("    Image {}x{}", bufferedImage.getWidth(), bufferedImage.getHeight());
            Pair<BufferedImage,Double> croppedImageData =
                    imageService.cropImage(albumPage.getImage(), bufferedImage, imageFrame.getImageRect().getDimension());
            imageFrame.setImage(croppedImageData.getLeft());
            //TODO use a real scoring here
            score += croppedImageData.getRight();

            ImageService.TextImageHolder textImageHolder =
                    imageService.getTextImage(albumPage.getText(), pageLayout.getHeight(), imageFrame.getTextRect());
            imageFrame.setTextImage(textImageHolder.getImage());
            score += calcTextScore(textImageHolder);
        }
        pageLayout.setScore(score);
        return pageLayout;
    }

    private double calcTextScore(ImageService.TextImageHolder imageHolder) {
        if (imageHolder.getActualFontSize() == imageHolder.getInitFontSize()) {
            double r = (double) imageHolder.getTextHeight() / (double) imageHolder.getFrameHeight();
            if (r > 0.33333) {
                return 1.0;
            } else {
                return r * 1.5;
            }
        } else {
            return 1.0 - (double) (imageHolder.getActualFontSize() - 10) / (double) (imageHolder.getInitFontSize() - 10);
        }
    }


}
