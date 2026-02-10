package com.julian.notificator.service.impl.tdt;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.julian.notificator.config.properties.TdtProperties;
import com.julian.notificator.model.tdt.TdtProgramme;
import com.julian.notificator.service.util.tdt.UtilTdt;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtresEpgService {

    private final TdtProperties tdtProperties;

    @Value("${rss.proxy-url3}")
    private String rssProxyUrl;

    public List<TdtProgramme> readAndFilter() {
        List<TdtProgramme> programmes = new ArrayList<>();

        Set<String> allowedChannels = new HashSet<>(tdtProperties.getAtresmedia());

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

            URL url = new URL(rssProxyUrl);
            try (InputStream inputStream = url.openStream()) {

                XMLEventReader reader = factory.createXMLEventReader(inputStream);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z");

                String currentChannel = null;
                String title = null;
                String description = null;
                String start = null;
                String stop = null;

                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();

                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();
                        String name = startElement.getName().getLocalPart();

                        switch (name) {
                            case "programme":
                                currentChannel = startElement.getAttributeByName(new QName("channel")).getValue();
                                start = startElement.getAttributeByName(new QName("start")).getValue();
                                stop = startElement.getAttributeByName(new QName("stop")).getValue();
                                title = null;
                                description = null;
                                break;
                            case "title":
                                event = reader.nextEvent();
                                title = event.asCharacters().getData();
                                break;
                            case "desc":
                                event = reader.nextEvent();
                                description = event.asCharacters().getData();
                                break;
                        }
                    }

                    createTdtProgramme(programmes, allowedChannels, formatter, currentChannel, title, description,
                            start, stop, event);
                }

                reader.close();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error leyendo el XML de Atresmedia desde URL: " + e.getMessage(), e);
        }

        return programmes;
    }

    private void createTdtProgramme(List<TdtProgramme> programmes, Set<String> allowedChannels,
            DateTimeFormatter formatter, String currentChannel, String title, String description, String start,
            String stop, XMLEvent event) {
        if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("programme")) {
            if (currentChannel != null && allowedChannels.contains(currentChannel)) {

                TdtProgramme programme = new TdtProgramme();
                programme.setChannelId(currentChannel);
                programme.setChannelDesc(UtilTdt.capitalize(currentChannel.replaceAll("\\.es$", "").toLowerCase()));
                programme.setTitle(title != null ? title : "");
                programme.setDesc(description != null ? description : "");
                programme.setStart(ZonedDateTime.parse(start, formatter));
                programme.setStop(ZonedDateTime.parse(stop, formatter));

                programmes.add(programme);
            }
        }
    }
}
