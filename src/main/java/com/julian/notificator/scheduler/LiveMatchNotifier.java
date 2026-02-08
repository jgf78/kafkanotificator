package com.julian.notificator.scheduler;

import java.util.List;

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
    private String lastStatus = null;

    public LiveMatchNotifier(
            FootballDataService footballDataService,
            @Qualifier("telegramServiceImpl") NotificationService notificationService) {
        this.footballDataService = footballDataService;
        this.telegramService = notificationService;
    }

    @Scheduled(fixedDelay = 30_000) // cada 30 segundos
    public void checkLiveMatch() {

        LiveMatchResponse response = footballDataService.getLiveStatus();
        Match match = null;

        if (response == null
                || response.getData() == null
                || response.getData().getMatches() == null
                || response.getData().getMatches().isEmpty()) {

            response = footballDataService.getFinishedMatch();
            List<Match> matches = response.getData().getMatches();
            match = matches.get(matches.size() - 1);
        }

        if (match == null) {
            match = response.getData().getMatches().get(0);
        }

        String currentStatus = match.getStatus();
        Integer home = match.getScore().getFullTime().getHome();
        Integer away = match.getScore().getFullTime().getAway();

        // ▶️ PRIMER TICK DEL PARTIDO → inicio
        if (lastMatchId == null || !lastMatchId.equals(match.getId())) {
            lastMatchId = match.getId();
            lastHomeScore = home;
            lastAwayScore = away;
            lastStatus = currentStatus;

            if ("IN_PLAY".equals(currentStatus)) {
                telegramService.sendMessage(buildKickoffMessage(match));
            }
            return;
        }
        
        // ⚽ GOL
        if (!home.equals(lastHomeScore) || !away.equals(lastAwayScore)) {
            telegramService.sendMessage(buildGoalMessage(match));
            lastHomeScore = home;
            lastAwayScore = away;
        }

        // 🔄 CAMBIO DE ESTADO
        changeState(match, currentStatus);
    }

    private void changeState(Match match, String currentStatus) {
        if (!currentStatus.equals(lastStatus)) {

            // 🟡 Descanso
            if ("PAUSED".equals(currentStatus)) {
                telegramService.sendMessage(buildHalftimeMessage(match));
            }

            // 🟢 Segunda parte
            if ("IN_PLAY".equals(currentStatus) && "PAUSED".equals(lastStatus)) {
                telegramService.sendMessage(buildSecondtimeMessage(match));
            }

            // 🏁 Final
            if ("FINISHED".equals(currentStatus)) {
                telegramService.sendMessage(buildFullTimeMessage(match));
                resetState();
                return;
            }

            lastStatus = currentStatus;
        }
    }

    private void resetState() {
        lastMatchId = null;
        lastHomeScore = null;
        lastAwayScore = null;
        lastStatus = null;
    }

    private String buildGoalMessage(Match match) {
        return String.format(
            "⚽ ¡Gol en el partido!%n%n%s %d - %d %s",
            match.getHomeTeam().getShortName(),
            match.getScore().getFullTime().getHome(),
            match.getScore().getFullTime().getAway(),
            match.getAwayTeam().getShortName()
        );
    }

    private String buildKickoffMessage(Match match) {
        return String.format(
            "🔔 ¡Empieza el partido!%n%n%s vs %s",
            match.getHomeTeam().getShortName(),
            match.getAwayTeam().getShortName()
        );
    }

    private String buildHalftimeMessage(Match match) {
        return String.format(
            "🟡 Descanso%n%n%s %d - %d %s",
            match.getHomeTeam().getShortName(),
            match.getScore().getFullTime().getHome(),
            match.getScore().getFullTime().getAway(),
            match.getAwayTeam().getShortName()
        );
    }

    private String buildSecondtimeMessage(Match match) {
        return String.format(
            "🟢 ¡Empieza la segunda parte!%n%n%s %d - %d %s",
            match.getHomeTeam().getShortName(),
            match.getScore().getFullTime().getHome(),
            match.getScore().getFullTime().getAway(),
            match.getAwayTeam().getShortName()
        );
    }

    private String buildFullTimeMessage(Match match) {
        return String.format(
            "🏁 Final del partido%n%n%s %d - %d %s",
            match.getHomeTeam().getShortName(),
            match.getScore().getFullTime().getHome(),
            match.getScore().getFullTime().getAway(),
            match.getAwayTeam().getShortName()
        );
    }
}
