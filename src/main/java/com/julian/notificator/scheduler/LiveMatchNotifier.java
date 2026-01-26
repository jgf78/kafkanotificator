package com.julian.notificator.scheduler;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.model.football.LiveMatchResponse;
import com.julian.notificator.model.football.Match;
import com.julian.notificator.service.FootballDataService;
import com.julian.notificator.service.NotificationService;

@Component
public class LiveMatchNotifier {

    private final FootballDataService footballDataService;
    private final NotificationService telegramService;

    private Integer lastHomeScore = null;
    private Integer lastAwayScore = null;
    private Long lastMatchId = null;

    public LiveMatchNotifier(FootballDataService footballDataService,
            @Qualifier("telegramServiceImpl") NotificationService notificationService) {
        this.footballDataService = footballDataService;
        this.telegramService = notificationService;
    }

    @Scheduled(fixedDelay = 30_000) // cada 30 segundos
    public void checkLiveMatch() {
        
        LiveMatchResponse response = footballDataService.getLiveStatus();

        if (response == null 
                || response.getData() == null 
                || response.getData().getMatches() == null 
                || response.getData().getMatches().isEmpty()) {
            resetState();
            return;
        }


        Match match = response.getData().getMatches().get(0);

        if (!isMatchActive(match.getStatus())) {
            resetState();
            return;
        }

        Integer home = match.getScore().getFullTime().getHome();
        Integer away = match.getScore().getFullTime().getAway();

        // Primer tick del partido
        if (lastMatchId == null || !lastMatchId.equals(match.getId())) {
            lastMatchId = match.getId();
            lastHomeScore = home;
            lastAwayScore = away;
            return;
        }

        // 🔔 CAMBIO DE MARCADOR
        if (!home.equals(lastHomeScore) || !away.equals(lastAwayScore)) {
            String msg = buildGoalMessage(match);
            telegramService.sendMessage(msg);

            lastHomeScore = home;
            lastAwayScore = away;
        }
    }

    private void resetState() {
        lastMatchId = null;
        lastHomeScore = null;
        lastAwayScore = null;
    }

    private String buildGoalMessage(Match match) {
        return String.format(
            "⚽ ¡Gol en el partido!%n%n%s %d - %d %s",
            match.getHomeTeam().getName(),
            match.getScore().getFullTime().getHome(),
            match.getScore().getFullTime().getAway(),
            match.getAwayTeam().getName()
        );
    }
    
    private boolean isMatchActive(String status) {
        return "IN_PLAY".equals(status) || "PAUSED".equals(status);
    }

}

