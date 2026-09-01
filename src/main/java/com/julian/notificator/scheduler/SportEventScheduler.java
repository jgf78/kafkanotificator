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

            log.info("Eventos encontrados: {}", events.size());

            events.forEach(event ->
                log.info("Evento: [{}] | enlaces: {}",
                        event.getMatchName(),
                        event.getLinks().size())
            );

            sportEventService.refreshHashes(events);

            log.info("Sport events refreshed. Total events: {}", events.size());

        } catch (Exception e) {

            log.error("Error refreshing sport events", e);

        }
    }

    private List<SportEvent> scrapeEvents() throws Exception {

        List<SportEvent> events = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .timeout(15000)
                .get();

        Elements rows = doc.select("#tbl tbody tr:not(.sep)");

        for (Element row : rows) {

            Elements cells = row.select("> td");

            if (cells.size() < 6) {
                continue;
            }

            String eventTime = cells.get(1).text().trim();
            String sport = cells.get(2).text().trim();
            String competition = cells.get(3).text().trim();
            String matchName = cells.get(4).text().trim();

            // Buscar si ya tenemos este evento
            SportEvent event = events.stream()
                    .filter(e ->
                            e.getEventTime().equals(eventTime)
                            && e.getMatchName().equals(matchName)
                    )
                    .findFirst()
                    .orElse(null);

            // Si no existe, lo creamos
            if (event == null) {

                event = SportEvent.builder()
                        .eventTime(eventTime)
                        .sport(sport)
                        .competition(competition)
                        .matchName(matchName)
                        .build();

                events.add(event);
            }

            // Añadir enlaces de esta fila
            List<String> links = cells.get(5)
                    .select("a[href]")
                    .stream()
                    .map(a -> a.attr("href").trim())
                    .filter(url -> !url.isEmpty())
                    .distinct()
                    .toList();

            for (String url : links) {

                boolean exists = event.getLinks().stream()
                        .anyMatch(link ->
                                link.getStreamUrl().equals(url)
                        );

                if (!exists) {
                    event.getLinks().add(
                            buildLink(event, url)
                    );
                }
            }
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