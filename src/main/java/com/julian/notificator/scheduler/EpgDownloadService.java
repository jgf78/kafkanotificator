package com.julian.notificator.scheduler;

import java.io.InputStream;
import java.net.URL;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.julian.notificator.config.properties.TdtProperties;
import com.julian.notificator.entity.TdtProgrammeEntity;
import com.julian.notificator.service.impl.tdt.EpgPersistService;
import com.julian.notificator.service.util.tdt.UtilTdt;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EpgDownloadService {

    @Value("${tdt.url}")
    private String epgUrl;

    private final TdtProperties tdtProperties;
    private final EpgPersistService persistService;

    @PostConstruct
    public void init() {
        saveEpgFromUrl();
    }

    @Scheduled(cron = "0 0 */2 * * *")
    public void downloadEpg() {
        saveEpgFromUrl();
    }

    public void saveEpgFromUrl() {
        log.info("📥 Descargando EPG desde {}", epgUrl);

        try (InputStream gzStream = new URL(epgUrl).openStream();
             GZIPInputStream xmlStream = new GZIPInputStream(gzStream)) {

            List<TdtProgrammeEntity> programmes = parseAndMap(xmlStream);

            if (!programmes.isEmpty()) {

                List<String> channelsNormalized = tdtProperties.getNationalChannels().stream()
                        .map(UtilTdt::normalizeChannel)
                        .toList();

                persistService.save(programmes, channelsNormalized);

                log.info("✅ EPG guardada en BBDD ({} programas)", programmes.size());
            } else {
                log.warn("⚠️ No se han encontrado programas para guardar");
            }

        } catch (Exception e) {
            log.error("❌ Error descargando/parsing EPG", e);
        }
    }

    private List<TdtProgrammeEntity> parseAndMap(InputStream xmlStream) {
        List<TdtProgrammeEntity> batch = new ArrayList<>();
        try {
            String xmlText = new String(xmlStream.readAllBytes());
            xmlText = xmlText.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;)", "&amp;");
            InputStream cleanedStream = new java.io.ByteArrayInputStream(xmlText.getBytes());

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(cleanedStream);
            TdtProgrammeEntity current = null;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    String localName = reader.getLocalName();

                    if ("programme".equals(localName)) {
                        current = new TdtProgrammeEntity();
                        String channel = reader.getAttributeValue(null, "channel");
                        current.setChannelId(channel);
                        current.setChannelNormalized(UtilTdt.normalizeChannel(channel));
                        current.setStartTime(parseDate(reader.getAttributeValue(null, "start"))); // UTC
                        current.setEndTime(parseDate(reader.getAttributeValue(null, "stop")));   // UTC

                    } else if ("title".equals(localName) && current != null) {
                        reader.next();
                        current.setTitle(cleanText(reader.getText()));

                    } else if ("desc".equals(localName) && current != null) {
                        reader.next();
                        current.setDescription(cleanText(reader.getText()));
                    }
                }

                if (event == XMLStreamConstants.END_ELEMENT &&
                    "programme".equals(reader.getLocalName()) &&
                    current != null) {

                    final TdtProgrammeEntity programmeToCheck = current; // variable final para lambda

                    boolean keep = tdtProperties.getNationalChannels().stream()
                            .map(UtilTdt::normalizeChannel)
                            .anyMatch(n -> n.equals(programmeToCheck.getChannelNormalized()));

                    if (keep &&
                        programmeToCheck.getTitle() != null &&
                        programmeToCheck.getStartTime() != null &&
                        programmeToCheck.getEndTime() != null) {
                        batch.add(programmeToCheck);
                    }

                    current = null;
                }
            }

            log.info("📺 EPG procesada y lista para guardar ({} programas filtrados)", batch.size());

        } catch (Exception e) {
            log.error("❌ Error parseando y filtrando EPG", e);
        }

        return batch;
    }

    private java.time.ZonedDateTime parseDate(String dateStr) {
        try {
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z");
            return java.time.ZonedDateTime.parse(dateStr, formatter)
                    .withZoneSameInstant(ZoneOffset.UTC); 
        } catch (Exception e) {
            log.warn("⚠️ Error parseando fecha: {}", dateStr, e);
            return null;
        }
    }

    private String cleanText(String text) {
        if (text == null) return null;
        return text.replaceAll("[\\u0000-\\u001F]", "").trim();
    }
}
