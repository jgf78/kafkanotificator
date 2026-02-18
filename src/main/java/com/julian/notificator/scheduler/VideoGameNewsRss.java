package com.julian.notificator.scheduler;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.videogames.RssFeed;
import com.julian.notificator.model.videogames.RssItem;
import com.julian.notificator.service.KafkaProducerService;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class VideoGameNewsRss {

    @Value("${rss.proxy-url5}")
    private String proxyUrl;
    
    private final KafkaProducerService kafkaProducerService;
    private final Set<String> sentGuids = new HashSet<>();

    @Scheduled(cron = "0 0 */3 * * *")
    public void checkFeed() {
        try {
            RssFeed feed = RssParser.parse(proxyUrl);

            for (RssItem item : feed.items()) {
                if (!sentGuids.add(item.guid())) continue; 
                String message = formatMessage(item);
                kafkaProducerService.sendMessage(message, DestinationType.DISCORD);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatMessage(RssItem item) {
        return String.format("""
                🚨 NUEVA NOTICIA GAMING 🚨

                🎮 %s

                📅 %s

                📝 %s

                🔗 %s

                #Gaming #InstantGaming
                """,
                item.title(),
                item.pubDate() != null ? item.pubDate().toLocalDate() : "Desconocida",
                item.description(),
                item.link()
        );
    }

    public static class RssParser {
        public static RssFeed parse(String url) throws Exception {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));

            List<RssItem> items = feed.getEntries().stream()
                    .map(entry -> new RssItem(
                            entry.getTitle(),
                            entry.getLink(),
                            entry.getDescription() != null ? entry.getDescription().getValue() : "",
                            entry.getPublishedDate() != null
                                    ? ZonedDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault())
                                    : null,
                            entry.getUri()
                    ))
                    .toList();

            return new RssFeed(
                    feed.getTitle(),
                    feed.getDescription(),
                    feed.getLink(),
                    feed.getPublishedDate() != null
                            ? ZonedDateTime.ofInstant(feed.getPublishedDate().toInstant(), ZoneId.systemDefault())
                            : null,
                    items
            );
        }
    }
}
