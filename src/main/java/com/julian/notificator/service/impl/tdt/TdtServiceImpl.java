package com.julian.notificator.service.impl.tdt;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.springframework.stereotype.Service;

import com.julian.notificator.config.properties.TdtProperties;
import com.julian.notificator.model.tdt.TdtProgramme;
import com.julian.notificator.scheduler.EpgDownloadService;
import com.julian.notificator.service.TdtCacheService;
import com.julian.notificator.service.TdtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TdtServiceImpl implements TdtService {

    private final TdtCacheService tdtCacheService;
    private final TdtProperties tdtProperties;
    private final EpgDownloadService epgDownloadService;

    @Override
    public List<TdtProgramme> getTvNow() {
        List<TdtProgramme> result = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));

        for (String channel : tdtProperties.getNationalChannels()) {

            TdtProgramme programme = tdtCacheService.getCachedNow(channel);

            if (programme == null) {
                programme = findCurrentProgrammeForChannel(channel, now);
                if (programme != null) {
                    tdtCacheService.cacheNow(programme);
                }
            }

            if (programme == null) {
                programme = new TdtProgramme();
                programme.setChannelId(channel);
                programme.setTitle("Sin programación");
            }

            result.add(programme);
        }

        return result;
    }

    private TdtProgramme findCurrentProgrammeForChannel(String channelId, ZonedDateTime now) {
        InputStream epgStream = epgDownloadService.getLastEpgStream();
        if (epgStream == null) {
            log.warn("⚠️ EPG aún no descargada");
            return null;
        }

        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(epgStream);
            TdtProgramme currentProgramme = null;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {

                    if ("programme".equals(reader.getLocalName())) {
                        String ch = reader.getAttributeValue(null, "channel");
                        if (!channelId.equals(ch)) {
                            currentProgramme = null;
                            continue;
                        }

                        currentProgramme = new TdtProgramme();
                        currentProgramme.setChannelId(ch);
                        currentProgramme.setStart(parseDate(reader.getAttributeValue(null, "start")));
                        currentProgramme.setStop(parseDate(reader.getAttributeValue(null, "stop")));
                    }

                    if ("title".equals(reader.getLocalName()) && currentProgramme != null) {
                        reader.next();
                        currentProgramme.setTitle(escapeXmlEntities(reader.getText()));
                    }

                    if ("desc".equals(reader.getLocalName()) && currentProgramme != null) {
                        reader.next();
                        currentProgramme.setDesc(escapeXmlEntities(reader.getText()));
                    }
                }

                if (event == XMLStreamConstants.END_ELEMENT &&
                        "programme".equals(reader.getLocalName()) &&
                        currentProgramme != null) {

                    if (now.isAfter(currentProgramme.getStart()) &&
                            now.isBefore(currentProgramme.getStop())) {
                        return currentProgramme; 
                    }

                    currentProgramme = null;
                }
            }

        } catch (Exception e) {
            log.error("❌ Error procesando la EPG para canal {}", channelId, e);
        }

        return null;
    }

    private String escapeXmlEntities(String text) {
        if (text == null) return null;
        return text.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;)", "&amp;");
    }

    private ZonedDateTime parseDate(String dateStr) {
        // El formato viene como "20260203065000 +0000"
        String pattern = "yyyyMMddHHmmss Z";
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern(pattern);
        return ZonedDateTime.parse(dateStr, formatter)
                .withZoneSameInstant(ZoneId.of("Europe/Madrid"));
    }

}