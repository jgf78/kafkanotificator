package com.julian.notificator.service.impl.news;

import java.io.IOException;
import java.net.URL;

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

        URL url = new URL(rssNewsUrl);

        SyndFeed feed;

        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(url)) {
            feed = input.build(reader);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📰 *Titulares del día*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        feed.getEntries().stream().limit(10).forEach(entry -> sb.append("• ").append(entry.getTitle()).append("\n"));

        sb.append("\n");

        sb.append(this.getSportHeadlines());

        return sb.toString();
    }

    private String getSportHeadlines() throws IllegalArgumentException, FeedException, IOException {

        URL url = new URL(rssSportUrl);

        SyndFeed feed;

        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(url)) {
            feed = input.build(reader);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("⚽ *Noticias Primera división*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        int count = 1;

        for (var entry : feed.getEntries().stream().limit(7).toList()) {

            String title = StringEscapeUtils.unescapeHtml4(entry.getTitle());

            String description = "";
            if (entry.getDescription() != null) {
                description = StringEscapeUtils.unescapeHtml4(entry.getDescription().getValue());
                description = description.replaceAll("<[^>]*>", "");   
                description = description.replaceAll("\\s+", " ").trim();
                description = truncate(description, 220);              
            }

            sb.append("*")
              .append(count++)
              .append(".* ")
              .append(title)
              .append("\n");

            if (!description.isBlank()) {
                sb.append("_")
                  .append(description)
                  .append("_\n");
            }

            sb.append("\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━");

        return sb.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength
                ? text.substring(0, maxLength) + "..."
                : text;
    }


}
