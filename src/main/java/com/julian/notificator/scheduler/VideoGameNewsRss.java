package com.julian.notificator.scheduler;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final KafkaProducerService kafkaProducerService;
    private final Set<String> sentGuids = new HashSet<>();

    @Scheduled(cron = "0 0 */3 * * *") 
    public void checkFeed() {
        try {
            URL feedUrl = new URL("http://192.168.1.3:9002/videojuegos.xml");
            var feed = RssParser.parse(feedUrl.toString());

            for (var item : feed.items()) {
                if (sentGuids.contains(item.guid())) continue; // ya enviado
                String message = formatMessage(item);
                kafkaProducerService.sendMessage(message, DestinationType.DISCORD);
                sentGuids.add(item.guid());
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
    
    public class RssParser {

        public static RssFeed parse(String url) throws Exception {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(url)));

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
                .collect(Collectors.toList());

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

