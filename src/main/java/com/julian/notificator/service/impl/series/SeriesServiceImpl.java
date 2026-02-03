package com.julian.notificator.service.impl.series;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
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
}
