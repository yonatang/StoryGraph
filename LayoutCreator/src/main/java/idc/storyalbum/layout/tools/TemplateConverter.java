package idc.storyalbum.layout.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import idc.storyalbum.layout.model.template.Frame;
import idc.storyalbum.layout.model.template.PageTemplate;
import idc.storyalbum.model.image.Rectangle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yonatan on 16/5/2015.
 */
@Slf4j
public class TemplateConverter {
    public static Set<PageTemplate> readTemplates(String templateDirName) {
        ObjectMapper objectMapper=new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        File templateDir = new File(templateDirName);
        log.info("Reading template dir {}", templateDir);

        Collection<File> files = FileUtils.listFiles(templateDir, new String[]{"xml"}, false);
        Set<PageTemplate> templates = new HashSet<>();
        for (File file : files) {
            try {
                PageTemplate pageTemplate = readTemplate(file);
                templates.add(pageTemplate);
                String jsonFile = FilenameUtils.removeExtension(file.getAbsolutePath()) + ".json";
                objectMapper.writeValue(new File(jsonFile), pageTemplate);
            } catch (Exception e) {
                log.warn("Error while parsing template {} - {}", file, e);
            }
        }
        log.info("Found {} templates", templates.size());
        return templates;
    }

    private static PageTemplate readTemplate(File file) throws Exception {
        PageTemplate pageTemplate = new PageTemplate();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        readPageLayout(pageTemplate, doc);
        readFrames(pageTemplate, doc);
        readTextFrames(pageTemplate,doc);
        log.debug("  {} is {}", file.getName(), pageTemplate);
        return pageTemplate;
    }

    private static void readTextFrames(PageTemplate pageTemplate, Document doc) {
        NodeList frameDetailsItms = doc.getElementsByTagName("textDetails");
        if (frameDetailsItms.getLength() != 1) {
            throw new RuntimeException("Cannot read pageLayoutDetails node - "+frameDetailsItms.getLength());
        }
        NodeList frameDetails = frameDetailsItms.item(0).getChildNodes();

        boolean riddleStoryHack = pageTemplate.getFrames().size()==9;
        log.debug("Riddle story hack? {}",riddleStoryHack);

        int idx=0;
        for (int i = 0; i < frameDetails.getLength(); i++) {

            Node frameDetail = frameDetails.item(i);
            if (frameDetail.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Frame frame = pageTemplate.getFrames().get(idx);
            if (riddleStoryHack && idx==2) {
                frame.setTextRect(new Rectangle(frame.getImageRect()));
                i--;
            } else {
                frame.setTextRect(new Rectangle());
                NodeList childNodes = frameDetail.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node childNode = childNodes.item(j);
                    switch (childNode.getNodeName()) {
                        case "topLeftX":
                            frame.getTextRect().setX(readNode(childNode));
                            break;
                        case "topLeftY":
                            frame.getTextRect().setY(readNode(childNode));
                            break;
                        case "width":
                            frame.getTextRect().setWidth(readNode(childNode));
                            break;
                        case "height":
                            frame.getTextRect().setHeight(readNode(childNode));
                            break;
                    }
                }
            }
            idx++;
        }
        if (idx!=pageTemplate.getFrames().size()){
            throw new RuntimeException("Missing text frame");
        }
    }
    private static void readFrames(PageTemplate pageTemplate, Document doc) {
        NodeList frameDetailsItms = doc.getElementsByTagName("frameDetails");
        if (frameDetailsItms.getLength() != 1) {
            throw new RuntimeException("Cannot read pageLayoutDetails node");
        }
        NodeList frameDetails = frameDetailsItms.item(0).getChildNodes();
        for (int i = 0; i < frameDetails.getLength(); i++) {

            Node frameDetail = frameDetails.item(i);
            if (frameDetail.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Frame frame = new Frame();
            pageTemplate.getFrames().add(frame);

            NodeList childNodes = frameDetail.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                switch (childNode.getNodeName()) {
                    case "topLeftX":
                        frame.getImageRect().setX(readNode(childNode));
                        break;
                    case "topLeftY":
                        frame.getImageRect().setY(readNode(childNode));
                        break;
                    case "width":
                        frame.getImageRect().setWidth(readNode(childNode));
                        break;
                    case "height":
                        frame.getImageRect().setHeight(readNode(childNode));
                        break;
                }
            }
        }


    }

    private static void readPageLayout(PageTemplate pageTemplate, Document doc) {
        NodeList pageLayoutDetails = doc.getElementsByTagName("pageLayoutDetails");
        if (pageLayoutDetails.getLength() != 1) {
            throw new RuntimeException("Cannot read pageLayoutDetails node");
        }
        Node pageLayoutDetail = pageLayoutDetails.item(0);
        NodeList childNodes = pageLayoutDetail.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            switch (child.getNodeName()) {
                case "height":
                    pageTemplate.setHeight(readNode(child));
                    break;
                case "width":
                    pageTemplate.setWidth(readNode(child));
                    break;
            }
        }
    }

    private static int readNode(Node child) {
        return Integer.parseInt(child.getFirstChild().getNodeValue());
    }

    public static void main(String... args) throws Exception {
//        String folderName="/Users/yonatan/Dropbox/Studies/Story Albums/Layouts/StoryBook/";
        String folderName="/Users/yonatan/Dropbox/Studies/Story Albums/Layouts/Poster-9/";
//        String folderName="/Users/yonatan/Dropbox/Studies/Story Albums/Layouts/StoryBook/";

        readTemplates(folderName);


    }
}
