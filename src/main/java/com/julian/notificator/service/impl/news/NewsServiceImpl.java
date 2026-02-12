package com.julian.notificator.service.impl.news;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.NewsService;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NewsServiceImpl implements NewsService {

    @Value("${rss.proxy-url}")
    private String rssNewsUrl;

    @Value("${rss.proxy-url4}")
    private String rssSportUrl;

    @Override
    public String getHeadlines() throws IllegalArgumentException, FeedException, IOException {

        log.info("📰 Descargando RSS noticias generales...");

        SyndFeed feed = loadFeedNoCache(rssNewsUrl);

        log.info("📰 RSS noticias descargado correctamente. Entradas recibidas: {}", feed.getEntries().size());

        if (!feed.getEntries().isEmpty()) {
            log.info("📰 Primer titular general: {}", feed.getEntries().get(0).getTitle());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📰 *Titulares del día*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        feed.getEntries().stream()
                .limit(10)
                .forEach(entry -> sb.append("• ")
                        .append(entry.getTitle())
                        .append("\n"));

        sb.append("\n");

        sb.append(this.getSportHeadlines());

        return sb.toString();
    }

    private String getSportHeadlines() throws IllegalArgumentException, FeedException, IOException {

        log.info("⚽ Descargando RSS noticias deportivas...");

        SyndFeed feed = loadFeedNoCache(rssSportUrl);

        log.info("⚽ RSS deportes descargado correctamente. Entradas recibidas: {}", feed.getEntries().size());

        if (!feed.getEntries().isEmpty()) {
            log.info("⚽ Primer titular deportivo: {}", feed.getEntries().get(0).getTitle());
        }

        StringBuilder sb = new StringBuilder();

        sb.append("⚽ *Noticias Primera División*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        int count = 1;

        for (var entry : feed.getEntries().stream().limit(7).toList()) {

            String title = StringEscapeUtils.unescapeHtml4(entry.getTitle());

            String description = "";
            if (entry.getDescription() != null) {

                description = entry.getDescription().getValue();

                description = StringEscapeUtils.unescapeHtml4(description);
                description = description.replaceAll("<[^>]*>", "");
                description = description.replace("&nbsp;", " ");
                description = description.replaceAll("\\s+", " ").trim();

                if (description.endsWith("Leer")) {
                    description = description.substring(0, description.length() - 5).trim();
                }

                description = truncate(description, 350);
            }

            sb.append("*")
              .append(count++)
              .append(".* ")
              .append(title)
              .append("\n");

            if (!description.isBlank()) {
                sb.append(description).append("\n");
            }

            sb.append("\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━");

        return sb.toString();
    }

    private SyndFeed loadFeedNoCache(String baseUrl) throws IOException, FeedException {

        String urlWithTimestamp = baseUrl + (baseUrl.contains("?") ? "&" : "?") 
                + "t=" + System.currentTimeMillis();

        log.info("🌐 URL solicitada: {}", urlWithTimestamp);

        URL url = new URL(urlWithTimestamp);
        URLConnection connection = url.openConnection();

        connection.setUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        SyndFeedInput input = new SyndFeedInput();

        try (XmlReader reader = new XmlReader(connection.getInputStream())) {
            return input.build(reader);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength
                ? text.substring(0, maxLength) + "..."
                : text;
    }
}
