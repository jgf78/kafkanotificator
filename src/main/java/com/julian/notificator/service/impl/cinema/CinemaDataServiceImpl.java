package com.julian.notificator.service.impl.cinema;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.julian.notificator.model.cinema.TmdbMovie;
import com.julian.notificator.service.CinemaDataService;

@Service
public class CinemaDataServiceImpl implements CinemaDataService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public CinemaDataServiceImpl(RestTemplate restTemplate, @Value("${cinema-data.base-url}") String baseUrl,
            @Value("${cinema-data.token}") String token) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.token = token;
    }

    @Override
    public List<TmdbMovie> getTop10NowPlaying() {

        TmdbNowPlayingResponse response = callApi();

        if (response == null || response.results() == null) {
            return List.of();
        }

        return response.results().stream().sorted(Comparator.comparingDouble(TmdbMovie::popularity).reversed())
                .limit(10).toList();
    }

    private TmdbNowPlayingResponse callApi() {

        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .queryParam("api_key", token)
                .queryParam("language", "es-ES")
                .queryParam("region", "ES")
                .toUriString();

        ResponseEntity<TmdbNowPlayingResponse> response = restTemplate.exchange(url, HttpMethod.GET, null,
                TmdbNowPlayingResponse.class);

        return response.getBody();
    }

    // =======================
    // MENSAJE PARA TELEGRAM
    // =======================

    @Override
    public String buildCarteleraMessage(List<TmdbMovie> movies) {

        StringBuilder sb = new StringBuilder();

        sb.append("🎬 *Cartelera actual – Top 10 más populares* 🇪🇸\n");
        sb.append("🔥 Ahora mismo en los cines\n\n");

        int ranking = 1;

        for (TmdbMovie movie : movies) {

            sb.append("⭐ *").append(ranking++).append(". ").append(escapeMarkdown(movie.title())).append("*\n");

            String overview = movie.overview();
            if (overview == null || overview.isBlank()) {
                overview = "_Sinopsis no disponible_";
            }

            sb.append("📖 ").append(escapeMarkdown(shorten(overview, 300))).append("\n");

            sb.append("📅 Estreno: ").append(formatDate(movie.releaseDate())).append("\n\n");
        }

        return sb.toString();
    }

    private String escapeMarkdown(String text) {
        return text.replace("_", "\\_").replace("*", "\\*").replace("[", "\\[").replace("]", "\\]");
    }

    private String formatDate(String date) {
        if (date == null || date.isBlank()) {
            return "Desconocida";
        }
        return LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String shorten(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength) + "…" : text;
    }
}
