package com.julian.notificator.scheduler;

import java.io.InputStream;
import java.net.URL;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.julian.notificator.config.properties.TdtProperties;
import com.julian.notificator.entity.TdtProgrammeEntity;
import com.julian.notificator.model.tdt.TdtProgramme;
import com.julian.notificator.service.impl.tdt.AtresEpgService;
import com.julian.notificator.service.impl.tdt.EpgPersistService;
import com.julian.notificator.service.util.tdt.UtilTdt;

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
    private final AtresEpgService atresEpgService;

    @Scheduled(cron = "0 0 */5 * * *")
    public void downloadEpg() {
        saveEpgFromUrl();
    }

    public void saveEpgFromUrl() {
        log.info("📥 Descargando EPG Atresmedia... ");
        List<TdtProgrammeEntity> programmes = new ArrayList<>();

        try {
            List<TdtProgramme> atresProgrammes = atresEpgService.readAndFilter();
            programmes.addAll(covertTdtProgrammeToTdtProgrammeEntity(atresProgrammes));
        } catch (Exception e) {
            log.warn("⚠️ No se pudo descargar el XML de Atresmedia (limite diario o XML inválido). Se omite este feed.", e);
        }

        log.info("📥 Descargando EPG resto de TDT desde {}", epgUrl);

        try (InputStream gzStream = new URL(epgUrl).openStream();
             GZIPInputStream xmlStream = new GZIPInputStream(gzStream)) {

            List<TdtProgrammeEntity> tdtProgrammes = parseAndMap(xmlStream);
            programmes.addAll(tdtProgrammes); 

            if (!programmes.isEmpty()) {
                persistService.save(programmes);
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

            XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

            XMLStreamReader reader = factory.createXMLStreamReader(xmlStream);

            Set<String> allowedChannels = tdtProperties.getNationalChannels()
                    .stream()
                    .map(UtilTdt::normalizeChannel)
                    .collect(Collectors.toSet());

            TdtProgrammeEntity current = null;

            while (reader.hasNext()) {

                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {

                    String name = reader.getLocalName();

                    if ("programme".equals(name)) {

                        current = new TdtProgrammeEntity();

                        String channel = reader.getAttributeValue(null, "channel");

                        current.setChannelId(channel);
                        current.setChannelNormalized(UtilTdt.normalizeChannel(channel));
                        current.setStartTime(parseDate(reader.getAttributeValue(null, "start")));
                        current.setEndTime(parseDate(reader.getAttributeValue(null, "stop")));

                    } 
                    else if ("title".equals(name) && current != null) {

                        current.setTitle(cleanText(reader.getElementText()));

                    } 
                    else if ("desc".equals(name) && current != null) {

                        current.setDescription(cleanText(reader.getElementText()));
                    }
                }

                if (event == XMLStreamConstants.END_ELEMENT &&
                    "programme".equals(reader.getLocalName()) &&
                    current != null) {

                    if (allowedChannels.contains(current.getChannelNormalized()) &&
                        current.getTitle() != null &&
                        current.getStartTime() != null &&
                        current.getEndTime() != null) {

                        batch.add(current);
                    }

                    current = null;
                }
            }

            log.info("📺 EPG procesada y filtrada ({} programas)", batch.size());

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
    
    private List<TdtProgrammeEntity> covertTdtProgrammeToTdtProgrammeEntity(List<TdtProgramme> atresProgrammes) {
        List<TdtProgrammeEntity> batch = new ArrayList<>();
        try {
            for (TdtProgramme prog : atresProgrammes) {
                TdtProgrammeEntity entity = new TdtProgrammeEntity();
                entity.setChannelId(prog.getChannelId());
                entity.setChannelNormalized(UtilTdt.normalizeChannel(prog.getChannelDesc()));
                entity.setTitle(prog.getTitle());
                entity.setDescription(prog.getDesc());
                entity.setStartTime(prog.getStart());
                entity.setEndTime(prog.getStop());

                batch.add(entity);
            }
        } catch (Exception e) {
            log.error("❌ Error parseando XML Atresmedia", e);
        }
        return batch;
    }
}