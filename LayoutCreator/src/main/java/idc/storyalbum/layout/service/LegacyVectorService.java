package idc.storyalbum.layout.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yonatan on 18/5/2015.
 */
@Service
@Slf4j
public class LegacyVectorService {
    @Getter
    public static class SalientSum {
        private File file;
        private List<Double> horizontalVector = new ArrayList<>();
        private List<Double> sumsHorz = new ArrayList<>();
        private List<Double> verticalVector = new ArrayList<>();
        private List<Double> sumsVert = new ArrayList<>();

    }

    @Cacheable("vector-cache")
    public SalientSum readVector(File file) throws Exception {
        log.info("Reading vector file {}", file);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        SalientSum ss = new SalientSum();
        // seems the files got mixed up with the data.
        readVectorList(doc, "horizontalVector", ss.verticalVector, ss.sumsVert);
        readVectorList(doc, "verticalVector", ss.horizontalVector, ss.sumsHorz);
        ss.file = file;
        return ss;
    }


    private void readVectorList(Document doc, String tagName, List<Double> list, List<Double> sums) {
        double sum = 0;
        NodeList verticalVector = doc.getElementsByTagName(tagName).item(0).getChildNodes();
        for (int i = 0; i < verticalVector.getLength(); i++) {
            Node item = verticalVector.item(i);
            if (item.getNodeType() == 1) {
                Node item1 = item.getChildNodes().item(0);
                double val = Double.parseDouble(item1.getNodeValue());
                list.add(val);
                sum += val;
                sums.add(sum);
            }
        }
    }

    public double calcWindow(SalientSum ss, boolean xDirection, int fromInclusive, int toInclusive) {
        try {
            List<Double> sum = xDirection ? ss.sumsHorz : ss.sumsVert;
            double from;
            double to;
            if (fromInclusive == 0) {
                from = 0;
            } else {
                from = sum.get(fromInclusive - 1);
            }
            to = sum.get(toInclusive);
            return to - from;
        } catch (RuntimeException e) {
            log.error("Error whilte doing from {} to {}, xDirection {} with {}", fromInclusive, toInclusive, xDirection, ss.file);
            throw e;
        }
    }
}
