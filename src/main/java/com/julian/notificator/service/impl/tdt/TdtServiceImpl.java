package com.julian.notificator.service.impl.tdt;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, List<TdtProgramme>> lastParsedEpg = new HashMap<>();

    @Override
    public List<TdtProgramme> getTvNow() {

        parseEpgStreamOnce();

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

    private void parseEpgStreamOnce() {
        InputStream epgStream = epgDownloadService.getLastEpgStream();
        if (epgStream == null) {
            log.warn("⚠️ EPG aún no descargada");
            return;
        }

        try {
            String xmlText = new String(epgStream.readAllBytes());
            xmlText = xmlText.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;)", "&amp;");

            InputStream cleanedStream = new java.io.ByteArrayInputStream(xmlText.getBytes());

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(cleanedStream);
            Map<String, List<TdtProgramme>> map = new HashMap<>();
            TdtProgramme currentProgramme = null;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    switch (reader.getLocalName()) {
                        case "programme" -> {
                            currentProgramme = new TdtProgramme();
                            currentProgramme.setChannelId(reader.getAttributeValue(null, "channel"));
                            currentProgramme.setStart(parseDate(reader.getAttributeValue(null, "start")));
                            currentProgramme.setStop(parseDate(reader.getAttributeValue(null, "stop")));
                        }
                        case "title" -> {
                            if (currentProgramme != null) {
                                reader.next();
                                currentProgramme.setTitle(escapeXmlEntities(reader.getText()));
                            }
                        }
                        case "desc" -> {
                            if (currentProgramme != null) {
                                reader.next();
                                currentProgramme.setDesc(escapeXmlEntities(reader.getText()));
                            }
                        }
                    }
                }

                if (event == XMLStreamConstants.END_ELEMENT &&
                        "programme".equals(reader.getLocalName()) &&
                        currentProgramme != null) {

                    map.computeIfAbsent(currentProgramme.getChannelId(), k -> new ArrayList<>())
                            .add(currentProgramme);

                    currentProgramme = null;
                }
            }

            lastParsedEpg = map;

        } catch (Exception e) {
            log.error("❌ Error parseando EPG", e);
        }
    }

    private TdtProgramme findCurrentProgrammeForChannel(String channelId, ZonedDateTime now) {
        if (lastParsedEpg == null || lastParsedEpg.isEmpty()) return null;

        String normalizedChannelId = normalizeChannelId(channelId);

        for (Map.Entry<String, List<TdtProgramme>> entry : lastParsedEpg.entrySet()) {
            String xmlChannel = entry.getKey();
            String normalizedXmlChannel = normalizeChannelId(xmlChannel);

            if (normalizedChannelId.equals(normalizedXmlChannel)) {
                List<TdtProgramme> programmes = entry.getValue();

                for (TdtProgramme p : programmes) {
                    if (p.getStart() != null && p.getStop() != null &&
                            now.isAfter(p.getStart()) && now.isBefore(p.getStop())) {
                        return p;
                    }
                }
            }
        }

        return null;
    }

    private String normalizeChannelId(String channel) {
        if (channel == null) return "";
        return channel.replaceAll("\\s|\\.", "").toLowerCase();
    }


    private String escapeXmlEntities(String text) {
        if (text == null) return null;
        return text.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;)", "&amp;");
    }

    private ZonedDateTime parseDate(String dateStr) {
        // Formato ejemplo: "20260203234000 +0000"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z");
        return ZonedDateTime.parse(dateStr, formatter)
                .withZoneSameInstant(ZoneId.of("Europe/Madrid"));
    }

    @Override
    public String buildTdtMessage(List<TdtProgramme> tvNow) {
        StringBuilder sb = new StringBuilder();

        sb.append("📺 *Programación actual – TV Nacional*\n");
        sb.append("⏰ Ahora mismo en emisión:\n\n");

        for (TdtProgramme programme : tvNow) {

            String channel = programme.getChannelId();
            if (channel != null && channel.endsWith(".TV")) {
                channel = channel.substring(0, channel.length() - 3); 
            }
            sb.append("📺 *").append(escapeMarkdown(channel)).append("*\n");

            String title = programme.getTitle() != null ? programme.getTitle() : "Sin programación";
            sb.append("_").append(escapeMarkdown(title)).append("_\n");

            if (programme.getStart() != null && programme.getStop() != null) {
                sb.append("🕒 ").append(formatTime(programme.getStart()))
                  .append(" – ").append(formatTime(programme.getStop())).append("\n");
            }

            sb.append("────────────────\n");
        }

        return sb.toString();
    }


    private String formatTime(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneId.of("Europe/Madrid"))
                   .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }

}
