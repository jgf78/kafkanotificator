package com.julian.notificator.service.impl.series;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.series.ShowResponse;
import com.julian.notificator.model.series.StreamingPlatform;
import com.julian.notificator.model.series.TopSeries;
import com.julian.notificator.service.SeriesService;

@Service
public class SeriesServiceImpl implements SeriesService {

    private final RestTemplate restTemplate;

    @Value("${rapidapi.streaming.base-url}")
    private String baseUrl;

    @Value("${rapidapi.key}")
    private String apiKey;

    public SeriesServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "topSeriesByPlatform", key = "#platform")
    public List<TopSeries> getTopByPlatform(StreamingPlatform platform) {

        StreamingPlatform streamingPlatform = StreamingPlatform.from(platform.apiValue());

        String url = String.format(
            "%s/shows/top?country=es&service=%s&show_type=series&output_language=es",
            baseUrl,
            streamingPlatform.apiValue()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", apiKey);
        headers.set("X-RapidAPI-Host", "streaming-availability.p.rapidapi.com");

        ShowResponse[] response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ShowResponse[].class
        ).getBody();

        if (response == null || response.length == 0) {
            return List.of();
        }

        return IntStream.range(0, Math.min(10, response.length))
                .mapToObj(i -> mapToDto(response[i], i + 1, streamingPlatform))
                .toList();
    }

    private TopSeries mapToDto(
            ShowResponse show,
            int position,
            StreamingPlatform platform
    ) {
        return new TopSeries(
                position,
                show.getTitle(),
                show.getFirstAirYear(),
                show.getRating(),
                show.getGenres()
                        .stream()
                        .map(g -> g.getName())
                        .toList(),
                platform.name()
        );
    }

    @Override
    public String buildSeriesMessage(List<TopSeries> series) {
        StringBuilder sb = new StringBuilder();

        sb.append("🎬 *Series actuales – Top 10 más populares* 🇪🇸\n");
        sb.append("🔥 Ahora mismo en la plataforma\n\n");

        int ranking = 1;

        for (TopSeries s : series) {

            sb.append("⭐ *").append(ranking++).append(". ").append(escapeMarkdown(s.title())).append("*\n");

            sb.append("📅 Año: ").append(s.year() > 0 ? s.year() : "Desconocido")
              .append(" | ⭐ Valoración: ").append(s.rating() >= 0 ? s.rating() + "%" : "N/A").append("\n");

            if (s.genres() != null && !s.genres().isEmpty()) {
                sb.append("🎭 Género: ").append(String.join(", ", s.genres())).append("\n");
            }

            if (s.platform() != null) {
                sb.append("📺 Disponible en: ").append(s.platform()).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                   .replace("*", "\\*")
                   .replace("[", "\\[")
                   .replace("]", "\\]");
    }

}
