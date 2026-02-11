package com.julian.notificator.service.impl.tracking;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.tracking.EventInfo;
import com.julian.notificator.model.tracking.TrackingInfo;
import com.julian.notificator.model.tracking.TrackingStatus;
import com.julian.notificator.service.TrackingService;
import com.julian.notificator.service.util.UtilString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TrackingServiceImpl implements TrackingService{

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${rapidapi.tracking.base-url}")
    private String baseUrl;
    
    @Value("${rapidapi.tracking.bearer}")
    private String bearer;

    @Value("${rapidapi.key}")
    private String apiKey;
    
    @Override
    public TrackingInfo getTracking(String trackCode) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("Authorization", "Bearer " + bearer);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + trackCode, HttpMethod.GET, entity,
                String.class);

        JsonNode root = mapper.readTree(response.getBody()).path("data");

        // Último evento
        JsonNode events = root.path("result").path("events");
        JsonNode lastEvent = events.get(events.size() - 1);

        EventInfo eventInfo = new EventInfo(lastEvent.path("action").asText(),
                LocalDateTime.parse(lastEvent.path("date").asText().replace(" ", "T")),
                lastEvent.path("service").asText());

        return new TrackingInfo(root.path("track").asText(), root.path("fromLocation").asText(),
                root.path("toLocation").asText(), root.path("dimensions").path("weight").asDouble(),
                root.path("dimensions").path("measure").asText(), root.path("status").asInt(), eventInfo);
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

        TrackingStatus status = TrackingStatus.fromCode(trackingInfo.status());
        sb.append("📊 *Estado:* ")
          .append(status.getDescription())
          .append("\n\n");

        if (trackingInfo.lastEvent() != null) {

            sb.append("🕒 *Último evento*\n");

            sb.append("📌 ")
              .append(UtilString.escapeMarkdown(trackingInfo.lastEvent().action()))
              .append("\n");

            sb.append("📅 ")
              .append(trackingInfo.lastEvent().date())
              .append("\n");

            sb.append("🚚 Servicio: ")
              .append(UtilString.escapeMarkdown(trackingInfo.lastEvent().service()))
              .append("\n");
        }

        return sb.toString();
    }
    
}
