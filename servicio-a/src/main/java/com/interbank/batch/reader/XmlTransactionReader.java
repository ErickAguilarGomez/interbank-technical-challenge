package com.interbank.batch.reader;

import com.interbank.common.dto.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class XmlTransactionReader implements ResourceAwareItemReaderItemStream<TransactionDto> {

    private static final Logger log = LoggerFactory.getLogger(XmlTransactionReader.class);

    private static final Pattern UNESCAPED_COTE_DIVOIRE = Pattern.compile("country=\"Cote D\"Ivoire\"");
    private static final String SANITIZED_COTE_DIVOIRE = "country=\"Cote D'Ivoire\"";

    private Resource resource;
    private boolean processed = false;
    private final DocumentBuilderFactory dbf;

    public XmlTransactionReader() {
        this.dbf = DocumentBuilderFactory.newInstance();
        this.dbf.setNamespaceAware(false);
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
        this.processed = false;
    }

    @Override
    public TransactionDto read() throws Exception {
        if (resource == null || processed) {
            return null;
        }

        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String xmlContent = new String(bytes, StandardCharsets.UTF_8);

            if (xmlContent.contains("country=\"Cote D\"Ivoire\"")) {
                xmlContent = UNESCAPED_COTE_DIVOIRE.matcher(xmlContent).replaceAll(SANITIZED_COTE_DIVOIRE);
            }

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlContent)));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            TransactionDto dto = new TransactionDto();

            NodeList personNodes = root.getElementsByTagName("person");
            if (personNodes.getLength() > 0) {
                Element person = (Element) personNodes.item(0);
                dto.setFirstname(person.getAttribute("firstname"));
                dto.setLastname(person.getAttribute("lastname"));
                dto.setCity(person.getAttribute("city"));
                dto.setCountry(person.getAttribute("country"));
                dto.setFirstname2(person.getAttribute("firstname2"));
                dto.setLastname2(person.getAttribute("lastname2"));
                dto.setEmail(person.getAttribute("email"));
            }

            dto.setRandomInt(getIntContent(root, "random"));
            dto.setRandomFloat(getDoubleContent(root, "random_float"));
            dto.setBoolFlag(getBooleanContent(root, "bool"));
            dto.setOperationDate(getDateContent(root, "date"));
            dto.setRegexPattern(getTextContent(root, "regEx"));
            dto.setChannelEnum(getTextContent(root, "enum"));

            NodeList eltNodes = root.getElementsByTagName("elt");
            List<String> elementsList = new ArrayList<>();
            for (int i = 0; i < eltNodes.getLength(); i++) {
                elementsList.add(eltNodes.item(i).getTextContent().trim());
            }
            dto.setElements(elementsList);

            NodeList ageNodes = root.getElementsByTagName("age");
            if (ageNodes.getLength() > 0) {
                String ageText = ageNodes.item(0).getTextContent().trim();
                try {
                    dto.setAge(Integer.parseInt(ageText));
                } catch (NumberFormatException e) {
                    log.warn("Invalid age value '{}' in file {}", ageText, resource.getFilename());
                }
            }

            processed = true;
            return dto;
        } catch (Exception e) {
            log.error("Failed to parse XML file {}: {}", resource.getFilename(), e.getMessage());
            processed = true;
            throw e;
        }
    }

    private String getTextContent(Element root, String tagName) {
        NodeList list = root.getElementsByTagName(tagName);
        if (list.getLength() > 0 && list.item(0) != null) {
            return list.item(0).getTextContent().trim();
        }
        return null;
    }

    private Integer getIntContent(Element root, String tagName) {
        String text = getTextContent(root, tagName);
        if (text != null && !text.isEmpty()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse integer '{}' for tag {}", text, tagName);
            }
        }
        return null;
    }

    private Double getDoubleContent(Element root, String tagName) {
        String text = getTextContent(root, tagName);
        if (text != null && !text.isEmpty()) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse double '{}' for tag {}", text, tagName);
            }
        }
        return null;
    }

    private Boolean getBooleanContent(Element root, String tagName) {
        String text = getTextContent(root, tagName);
        if (text != null && !text.isEmpty()) {
            return Boolean.parseBoolean(text);
        }
        return null;
    }

    private LocalDate getDateContent(Element root, String tagName) {
        String text = getTextContent(root, tagName);
        if (text != null && !text.isEmpty()) {
            try {
                return LocalDate.parse(text);
            } catch (Exception e) {
                log.warn("Failed to parse date '{}' for tag {}", text, tagName);
            }
        }
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.processed = false;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void close() throws ItemStreamException {
        this.resource = null;
        this.processed = false;
    }
}
