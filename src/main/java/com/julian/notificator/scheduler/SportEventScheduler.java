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

    private static final String URL = "http://mistreamt.duckdns.org:8091/deportesLIVE/";

    private final SportEventService sportEventService;

    //@Scheduled(cron = "0 0 0,12 * * *")
    @Scheduled(cron = "0 */5 * * * *")
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

        // La tabla de eventos es #tbl
        Elements rows = doc.select("#tbl tbody tr:not(.sep)");

        for (Element row : rows) {

            Elements cells = row.select("> td");

            // Una fila válida debe tener:
            // 0 = Día
            // 1 = Hora
            // 2 = Deporte
            // 3 = Competición
            // 4 = Partido
            // 5 = Enlaces
            if (cells.size() < 6) {
                continue;
            }

            SportEvent event = SportEvent.builder()
                    .eventTime(cells.get(1).text())
                    .sport(cells.get(2).text())
                    .competition(cells.get(3).text())
                    .matchName(cells.get(4).text())
                    .build();

            List<String> links = cells.get(5)
                    .select("a[href^=acestream\\:\\/\\/]")
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
