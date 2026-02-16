package com.julian.notificator.service.impl.tracking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.tracking.EventInfo;
import com.julian.notificator.model.tracking.TrackingInfo;
import com.julian.notificator.service.TrackingService;
import com.julian.notificator.service.util.UtilString;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrackingServiceImpl implements TrackingService{

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${rapidapi.tracking.base-url}")
    private String baseUrl;
    
    @Value("${rapidapi.tracking.bearer}")
    private String bearer;

    @Value("${rapidapi.key}")
    private String apiKey;

    @Cacheable(value = "trackingOrder", key = "#trackCode")
    @Override
    public TrackingInfo getTracking(String trackCode) {

        try {
            log.info("📦 Consultando tracking para {}", trackCode);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", apiKey);
            headers.set("Authorization", "Bearer " + bearer);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .queryParam("track", trackCode)
                    .build()
                    .toUriString();


            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("⚠️ Respuesta no válida de RapidAPI");
            }

            JsonNode root = mapper.readTree(response.getBody()).path("data");

            if (root.isMissingNode()) {
                log.warn("No se encontraron datos para el envío");
            }

            JsonNode events = root.path("result").path("events");

            EventInfo eventInfo = null;

            if (events.isArray() && events.size() > 0) {
                JsonNode lastEvent = events.get(0);

                eventInfo = new EventInfo(
                        lastEvent.path("action").asText(),
                        LocalDateTime.parse(lastEvent.path("date").asText().replace(" ", "T")),
                        lastEvent.path("service").asText()
                );
            }

            return new TrackingInfo(
                    root.path("track").asText(),
                    root.path("fromLocation").asText(),
                    root.path("toLocation").asText(),
                    root.path("dimensions").path("weight").asDouble(),
                    root.path("dimensions").path("measure").asText(),
                    root.path("status").asInt(),
                    eventInfo
            );

        } catch (ResourceAccessException e) {
            log.error("⏱ Timeout llamando a RapidAPI", e);

        } catch (Exception e) {
            log.error("❌ Error inesperado consultando tracking", e);
        }
        return null;
    }

    @Override
    public String buildTrackingMessage(TrackingInfo trackingInfo) {

        StringBuilder sb = new StringBuilder();

        sb.append("📦 *Seguimiento del envío*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        sb.append("🔎 *Número:* ")
          .append(UtilString.escapeMarkdown(trackingInfo.trackNumber()))
          .append("\n");

        sb.append("📍 *Origen:* ")
          .append(UtilString.escapeMarkdown(trackingInfo.fromLocation()))
          .append("\n");

        sb.append("🏁 *Destino:* ")
          .append(UtilString.escapeMarkdown(trackingInfo.toLocation()))
          .append("\n");

        sb.append("⚖ *Peso:* ")
          .append(trackingInfo.weight())
          .append(" ")
          .append(UtilString.escapeMarkdown(trackingInfo.weightUnit()))
          .append("\n");

        String status = trackingInfo.lastEvent().action();
        sb.append("📊 *Estado:* ")
          .append(status)
          .append("\n\n");

        if (trackingInfo.lastEvent() != null) {

            sb.append("🕒 *Último evento*\n");

            sb.append("📌 ")
              .append(UtilString.escapeMarkdown(trackingInfo.lastEvent().action()))
              .append("\n");

            sb.append("📅 ")
              .append(trackingInfo.lastEvent().date().format(DATE_FORMATTER))
              .append("\n");

            sb.append("🚚 Servicio: ")
              .append(UtilString.escapeMarkdown(trackingInfo.lastEvent().service()))
              .append("\n");
        }

        return sb.toString();
    }
    
}
