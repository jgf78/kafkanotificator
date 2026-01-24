package com.julian.notificator.service.impl.football;

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

    
    @Value("${football-data.team-id}")
    private String teamId;

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public FootballDataServiceImpl(
            RestTemplate restTemplate,
            @Value("${football-data.base-url}") String baseUrl,
            @Value("${football-data.token}") String token) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.token = token;
    }

    @Override
    public LiveMatchResponse getLiveStatus() {

        String url = String.format(
                "%s/teams/%s/matches?status=IN_PLAY&competitions=PD,CL",
                baseUrl,
                teamId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<FootballData> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        FootballData.class
                );

        FootballData body = response.getBody();

        LiveMatchResponse result = new LiveMatchResponse();

        if (body == null || body.getMatches() == null || body.getMatches().isEmpty()) {
            result.setPlaying(false);
            result.setMessage("El Real Madrid no está jugando ningún partido ahora mismo ⚽");
            result.setData(null);
            return result;
        }

        Match match = body.getMatches().get(0);
        result.setPlaying(true);
        
        // Construimos el mensaje principal
        StringBuilder msg = new StringBuilder();

        // Competición
        msg.append("🏆 ")
           .append(match.getCompetition().getName())
           .append("\n\n");

        // Marcador
        msg.append("⚽ Partido en juego: ")
           .append(match.getHomeTeam().getName())
           .append(" ")
           .append(match.getScore().getFullTime().getHome())
           .append(" - ")
           .append(match.getScore().getFullTime().getAway())
           .append(" ")
           .append(match.getAwayTeam().getName())
           .append("\n");

        // Estado del partido
        msg.append("⏱ Estado: ")
           .append(getMatchState(match))
           .append("\n");

        // Árbitro (si viene informado)
        if (match.getReferees() != null && !match.getReferees().isEmpty()) {
            msg.append("⚫ Árbitro: ")
               .append(match.getReferees().get(0).getName())
               .append("\n");
        }

        result.setMessage(msg.toString());
        result.setData(body);


        return result;

    }

    private String getMatchState(Match match) {
        String estado;
        switch (match.getStatus()) {
            case "IN_PLAY":
                estado = "En juego";
                break;
            case "FINISHED":
                estado = "Finalizado";
                break;
            case "SCHEDULED":
                estado = "Pendiente";
                break;
            default:
                estado = match.getStatus().replace("_", " ");
                break;
        }
        return estado;
    }
    
    @Override
    public String formatLiveMatchMessage(FootballData response) {
        if (response == null || response.getMatches() == null || response.getMatches().isEmpty()) {
            return "⚽ El Real Madrid no está jugando ningún partido en este momento.";
        }

        Match match = response.getMatches().get(0); // tomamos el primer partido en juego

        StringBuilder msg = new StringBuilder();

        // Marcador
        msg.append("⚽ Partido en juego: ")
           .append(match.getHomeTeam().getName())
           .append(" ")
           .append(match.getScore().getFullTime().getHome())
           .append(" - ")
           .append(match.getScore().getFullTime().getAway())
           .append(" ")
           .append(match.getAwayTeam().getName())
           .append("\n");

        // Estado del partido
        msg.append("⏱ Estado: ")
           .append(getMatchState(match))
           .append("\n");

        // Árbitro 
        if (match.getReferees() != null && !match.getReferees().isEmpty()) {
            msg.append("⚫ Árbitro: ")
               .append(match.getReferees().get(0).getName())
               .append("\n");
        }

        return msg.toString();
    }


}
