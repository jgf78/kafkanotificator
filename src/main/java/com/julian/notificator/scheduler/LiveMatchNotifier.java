package com.julian.notificator.scheduler;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.model.football.LiveMatchResponse;
import com.julian.notificator.model.football.Match;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.service.FootballDataService;
import com.julian.notificator.service.NotificationService;
import com.julian.notificator.service.SubscriberService;
import com.julian.notificator.service.util.Constants;

@Component
public class LiveMatchNotifier {

    private final FootballDataService footballDataService;
    private final NotificationService telegramService;
    private final SubscriberService subscriberService;

    private Integer lastHomeScore = null;
    private Integer lastAwayScore = null;
    private Long lastMatchId = null;
    private String lastStatus = null;

    public LiveMatchNotifier(
            FootballDataService footballDataService,
            @Qualifier("telegramServiceImpl") NotificationService notificationService,
            SubscriberService subscriberService) {
        this.footballDataService = footballDataService;
        this.telegramService = notificationService;
        this.subscriberService = subscriberService;
    }

    @Scheduled(fixedDelay = 15_000)
    public void checkLiveMatch() {

        LiveMatchResponse response = footballDataService.getLiveStatus();

        // ❌ Si la API falla o no hay partidos → no hacemos nada
        if (response == null || response.getData() == null
                || response.getData().getMatches() == null
                || response.getData().getMatches().isEmpty()) {
            return;
        }

        Match match = response.getData().getMatches().get(0);

        String currentStatus = match.getStatus();
        Integer home = match.getScore().getFullTime().getHome();
        Integer away = match.getScore().getFullTime().getAway();

        // 🧠 Primera ejecución
        if (lastMatchId == null) {
            lastMatchId = match.getId();
            lastHomeScore = home;
            lastAwayScore = away;
            lastStatus = currentStatus;

            if ("IN_PLAY".equals(currentStatus)) {
                sendNotificationToAll(buildKickoffMessage(match));
            }
            return;
        }

        // ⚠️ Cambio de partido sospechoso (API glitch)
        if (!lastMatchId.equals(match.getId())) {

            if (!"FINISHED".equals(lastStatus)) {
                return;
            }

            // Nuevo partido válido
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

        if (lastStatus == null || currentStatus.equals(lastStatus)) {
            return;
        }

        // 🟡 Descanso
        if ("PAUSED".equals(currentStatus)) {
            sendNotificationToAll(buildHalftimeMessage(match));
        }

        // 🟢 Segunda parte
        if ("IN_PLAY".equals(currentStatus) && "PAUSED".equals(lastStatus)) {
            sendNotificationToAll(buildSecondtimeMessage(match));
        }

        // 🔔 Inicio REAL del partido 
        if ("IN_PLAY".equals(currentStatus)
                && ("SCHEDULED".equals(lastStatus)
                || "TIMED".equals(lastStatus))) {

            sendNotificationToAll(buildKickoffMessage(match));
        }

        // 🏁 Final
        if ("FINISHED".equals(currentStatus)) {
            sendNotificationToAll(buildFullTimeMessage(match));
        }

        lastStatus = currentStatus;
    }

    private void sendNotificationToAll(String message) {
        telegramService.sendMessage(message, DestinationTelegramType.ALL);
        subscriberService.notifyAllSubscribers(Constants.LIVE_MATCH_EVENT, message);
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
