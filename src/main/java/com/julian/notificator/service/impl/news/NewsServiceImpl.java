package com.julian.notificator.service.impl.news;

import java.io.IOException;
import java.net.URL;

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
    private String rssProxyUrl;
    
    @Override
    public String getHeadlines() throws IllegalArgumentException, FeedException, IOException {

        URL url = new URL(rssProxyUrl);

        SyndFeed feed;

        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(url)) {
            feed = input.build(reader);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📰 *Titulares del día*\n\n");

        feed.getEntries()
            .stream()
            .limit(10)
            .forEach(entry -> sb.append("• ")
                                .append(entry.getTitle())
                                .append("\n"));

        return sb.toString();
    }

}
