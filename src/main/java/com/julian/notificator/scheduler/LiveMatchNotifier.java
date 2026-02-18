package com.julian.notificator.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.entity.Subscribers;
import com.julian.notificator.model.football.LiveMatchResponse;
import com.julian.notificator.model.football.Match;
import com.julian.notificator.service.FootballDataService;
import com.julian.notificator.service.NotificationService;
import com.julian.notificator.service.SubscriberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class LiveMatchNotifier {

    private static final String LIVE_MATCH_EVENT = "LIVE_MATCH_EVENT";
    private final FootballDataService footballDataService;
    private final NotificationService telegramService;
    private final SubscriberService subscriberService;

    private Integer lastHomeScore = null;
    private Integer lastAwayScore = null;
    private Long lastMatchId = null;
    private String lastStatus = null;

    @Scheduled(fixedDelay = 20_000)
    public void checkLiveMatch() {

        LiveMatchResponse response = footballDataService.getLiveStatus();
        Match match = null;

        if (response == null || response.getData() == null
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
                sendNotificationToAll(buildKickoffMessage(match));
            }
            return;
        }

        // ⚽ GOL
        if (!home.equals(lastHomeScore) || !away.equals(lastAwayScore)) {
            sendNotificationToAll(buildGoalMessage(match));
            lastHomeScore = home;
            lastAwayScore = away;
        }

        // 🔄 CAMBIO DE ESTADO
        changeState(match, currentStatus);
    }

    private void changeState(Match match, String currentStatus) {
        if (!currentStatus.equals(lastStatus)) {

            if ("PAUSED".equals(currentStatus)) {
                sendNotificationToAll(buildHalftimeMessage(match));
            }

            if ("IN_PLAY".equals(currentStatus) && "PAUSED".equals(lastStatus)) {
                sendNotificationToAll(buildSecondtimeMessage(match));
            }

            if ("FINISHED".equals(currentStatus)) {
                sendNotificationToAll(buildFullTimeMessage(match));
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

    private void sendNotificationToAll(String message) {
        telegramService.sendMessage(message);

        List<Subscribers> subscribers = subscriberService.getActiveSubscribers();
        for (Subscribers s : subscribers) {
            subscriberService.notifyAllSubscribers(LIVE_MATCH_EVENT, message);
        }
    }

    // -------------------- Mensajes --------------------
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
