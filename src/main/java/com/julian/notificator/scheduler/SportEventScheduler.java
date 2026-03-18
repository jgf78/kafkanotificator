package com.julian.notificator.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.entity.SportEvent;
import com.julian.notificator.entity.SportEventLink;
import com.julian.notificator.service.SportEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SportEventScheduler {

    private static final String URL = "https://deportes-live.vercel.app/index.html";

    private final SportEventService sportEventService;

    @Scheduled(cron = "0 0 0,12 * * *") 
    public void refreshSportEvents() {
        log.info("Starting sport events refresh...");
        try {
            List<SportEvent> events = scrapeEvents();
            sportEventService.refreshHashes(events);
            log.info("Sport events refreshed. Total events: {}", events.size());
        } catch (Exception e) {
            log.error("Error refreshing sport events", e);
        }
    }

    private List<SportEvent> scrapeEvents() throws Exception {

        List<SportEvent> events = new ArrayList<>();

        Document doc = Jsoup.connect(URL).get();

        Elements rows = doc.select("tbody tr:not(.sep)");

        for (Element row : rows) {

            SportEvent event = SportEvent.builder()
                    .eventTime(row.select("td.hora").text())
                    .sport(row.select("td.dep").text())
                    .competition(row.select("td").get(3).text())
                    .matchName(row.select("td").get(4).text())
                    .build();

            List<String> links = row.select("td.links a")
                    .stream()
                    .map(a -> a.attr("href"))
                    .toList();

            links.forEach(url -> {
                event.getLinks().add(
                        buildLink(event, url)
                );
            });

            events.add(event);
        }

        return events;
    }

    private SportEventLink buildLink(SportEvent event, String url) {
        return SportEventLink.builder()
                .event(event)
                .streamUrl(url)
                .build();
    }
}
