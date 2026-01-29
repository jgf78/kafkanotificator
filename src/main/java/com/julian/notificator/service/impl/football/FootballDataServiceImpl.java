package com.julian.notificator.service.impl.football;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.football.FootballData;
import com.julian.notificator.model.football.LiveMatchResponse;
import com.julian.notificator.model.football.Match;
import com.julian.notificator.service.FootballDataService;

@Service
public class FootballDataServiceImpl implements FootballDataService {

    private static final String LIVE_MATCH = "%s/teams/%s/matches?status=IN_PLAY,PAUSED";
    private static final String NEXT_MATCH = "%s/teams/%s/matches?status=SCHEDULED";
    private static final String FINISHED_MATCH = "%s/teams/%s/matches?status=FINISHED";

    @Value("${football-data.team-id}")
    private String teamId;

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public FootballDataServiceImpl(RestTemplate restTemplate, @Value("${football-data.base-url}") String baseUrl,
            @Value("${football-data.token}") String token) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.token = token;
    }

    @Override
    public String getNextMatch() {
        FootballData body = callAPI(NEXT_MATCH);
        Match match = body.getMatches().get(0);
        StringBuilder msg = getFinalMessage(match);
        return msg.toString();
    }
    
    @Override
    public LiveMatchResponse getFinishedMatch() {
        FootballData body = callAPI(FINISHED_MATCH);
        LiveMatchResponse result = new LiveMatchResponse();
        result.setPlaying(false);
        result.setData(body);
        List<Match> matches = result.getData().getMatches();
        Match match = matches.get(matches.size() - 1);
        StringBuilder msg = getFinalMessage(match);
        result.setMessage(msg.toString());
        return result;
    }

    @Override
    public LiveMatchResponse getLiveStatus() {

        FootballData body = callAPI(LIVE_MATCH);

        LiveMatchResponse result = new LiveMatchResponse();

        if (body == null || body.getMatches() == null || body.getMatches().isEmpty()) {
            result.setPlaying(false);
            String messageNextMatch = getNextMatch();
            StringBuilder msg = new StringBuilder();
            msg.append("⚽ El Real Madrid no está jugando ningún partido ahora mismo ⚽");
            msg.append("\n");
            msg.append("Próximo partido: ");
            msg.append("\n");
            msg.append(messageNextMatch);
            result.setMessage(msg.toString());
            result.setData(null);
            return result;
        }

        Match match = body.getMatches().get(0);
        result.setPlaying(true);

        StringBuilder msg = getFinalMessage(match);

        result.setMessage(msg.toString());
        result.setData(body);

        return result;

    }

    private StringBuilder getFinalMessage(Match match) {
        // Construimos el mensaje principal
        StringBuilder msg = new StringBuilder();

        // Competición
        msg.append("🏆 ").append(match.getCompetition().getName()).append("\n");

        // Marcador
        msg.append(match.getHomeTeam().getShortName()).append(match.getScore().getFullTime().getHome() != null ? " "+match.getScore().getFullTime().getHome()
                        : "")
                .append(" - ")
                .append(match.getScore().getFullTime().getAway() != null ? match.getScore().getFullTime().getAway()
                        : "")
                .append(" ").append(match.getAwayTeam().getShortName()).append("\n");

        // Estado del partido
        msg.append("⏱ Estado: ").append(getMatchState(match)).append("\n");
        
        //Fecha y hora
        msg.append("📅 Fecha: ").append(formattedDate(match)).append("\n");
        
        // Árbitro (si viene informado)
        if (match.getReferees() != null && !match.getReferees().isEmpty()) {
            msg.append("⚫ Árbitro: ").append(match.getReferees().get(0).getName()).append("\n");
        }
        return msg;
    }

    private FootballData callAPI(String urlInput) {
        String url = String.format(urlInput, baseUrl, teamId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<FootballData> response = restTemplate.exchange(url, HttpMethod.GET, entity, FootballData.class);

        FootballData body = response.getBody();
        return body;
    }

    private String getMatchState(Match match) {
        String estado;
        switch (match.getStatus()) {
        case "IN_PLAY":
            estado = "En juego ";
            break;
        case "FINISHED":
            estado = "Finalizado ";
            break;
        case "SCHEDULED":
            estado = "Próximo ";
            break;
        case "PAUSED":
            estado = "Descanso ";
            break;
        case "TIMED":
            estado = "Programado ";
            break;
        default:
            estado = match.getStatus().replace("_", " ");
            break;
        }
        return estado;
    }

    @Override
    public String formatLiveMatchMessage() {
        return getLiveStatus().getMessage();
    }

    private String formattedDate(Match match) {
        return Instant.parse(match.getUtcDate()).atZone(ZoneId.of("Europe/Madrid"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

}
