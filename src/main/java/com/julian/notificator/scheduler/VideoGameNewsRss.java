package com.julian.notificator.scheduler;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.model.videogames.RssFeed;
import com.julian.notificator.model.videogames.RssItem;
import com.julian.notificator.service.NotificationService;
import com.julian.notificator.service.SubscriberService;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Component
public class VideoGameNewsRss {

    @Value("${rss.proxy-url5}")
    private String proxyUrl;
    
    private final NotificationService discordService;
    private final Set<String> sentGuids = new HashSet<>();
    
    public VideoGameNewsRss(
            @Qualifier("discordServiceImpl") NotificationService discordService,
            SubscriberService subscriberService) {
        this.discordService = discordService;
    }

    @Scheduled(cron = "0 0 */3 * * *")
    public void checkFeed() {
        try {
            RssFeed feed = RssParser.parse(proxyUrl);

            ZonedDateTime yesterdayLimit = ZonedDateTime.now().minusDays(1);

            feed.items().stream()
                    .filter(item -> item.pubDate() != null)
                    .filter(item -> item.pubDate().isAfter(yesterdayLimit)) // solo hoy o ayer
                    .sorted(Comparator.comparing(RssItem::pubDate).reversed()) // más recientes primero
                    .filter(item -> sentGuids.add(item.guid())) // evita duplicados
                    .limit(5) // máximo 5
                    .forEach(item -> {
                        String message = formatMessage(item);
                        discordService.sendMessage(message);
                    });

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
