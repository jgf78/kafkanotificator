
package com.julian.notificator.service.impl.calendarofsaints;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.julian.notificator.model.calendarofsaints.CalendarOfSaintsResponse;
import com.julian.notificator.service.CalendarOfSaintsService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalendarOfSaintsServiceImpl implements CalendarOfSaintsService {

    private static final String API_URL =
            "https://nameday.abalin.net/api/V2/date";

    private static final String COUNTRY = "es";

    private static final ZoneId TIME_ZONE = ZoneId.of("Europe/Madrid");

    private final RestTemplate backendRestTemplate;

    public CalendarOfSaintsServiceImpl(
            @Qualifier("backendRestTemplate") RestTemplate backendRestTemplate) {
        this.backendRestTemplate = backendRestTemplate;
    }

    @Override
    public CalendarOfSaintsResponse getSaints() {

        LocalDate today = LocalDate.now(TIME_ZONE);

        String url = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("day", today.getDayOfMonth())
                .queryParam("month", today.getMonthValue())
                .queryParam("country", COUNTRY)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        NamedayApiResponse response = backendRestTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                NamedayApiResponse.class
        ).getBody();

        if (response == null || response.data() == null) {
            log.warn("No se han obtenido datos del santoral para {}/{}",
                    today.getDayOfMonth(), today.getMonthValue());

            return new CalendarOfSaintsResponse(false, null);
        }

        String saints = response.data().spain();

        log.info("Santoral obtenido para {}/{}: {}",
                today.getDayOfMonth(), today.getMonthValue(), saints);

        return new CalendarOfSaintsResponse(
                response.success(),
                saints
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NamedayApiResponse(
            boolean success,
            String message,
            NamedayData data
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NamedayData(
            @JsonProperty("es") String spain
    ) {
    }
}
