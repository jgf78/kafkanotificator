package com.julian.notificator.service.impl.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.transport.TelegramStop;
import com.julian.notificator.service.TransportService;
import com.julian.notificator.service.util.UtilString;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransportServiceImpl implements TransportService {

    private static final String OVERPASS_URL =
            "https://overpass-api.de/api/interpreter";

    private static final String USER_AGENT =
            "Notificator/1.0 (contacto: julian.rss.android@gmail.com)";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TransportServiceImpl(
            @org.springframework.beans.factory.annotation.Qualifier("overpassRestTemplate")
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TelegramStop> getStopsNearby(
            String latitude,
            String longitude,
            int radius) {

        log.info(
                "Buscando transportes cercanos a latitud: {}, longitud: {}, radio: {} metros",
                latitude, longitude, radius);

        try {
            String query = String.format(
                    "[out:json][timeout:25];"
                            + "(node(around:%d,%s,%s)[\"highway\"=\"bus_stop\"];"
                            + "node(around:%d,%s,%s)[\"railway\"~\"station|halt|tram_stop|subway_entrance\"];"
                            + "node(around:%d,%s,%s)[\"public_transport\"=\"platform\"];"
                            + ");out body;",
                    radius, latitude, longitude,
                    radius, latitude, longitude,
                    radius, latitude, longitude);

            log.info("Consulta transportes: {}", query);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(
                    Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(HttpHeaders.USER_AGENT, USER_AGENT);

            MultiValueMap<String, String> formData =
                    new LinkedMultiValueMap<>();
            formData.add("data", query);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(formData, headers);

            log.info("Enviando petición POST a Overpass: {}", OVERPASS_URL);

            ResponseEntity<String> httpResponse = restTemplate.exchange(
                    OVERPASS_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            String response = httpResponse.getBody();

            if (response == null || response.isBlank()) {
                log.warn("Overpass ha devuelto una respuesta vacía");
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode elements = root.path("elements");

            List<TelegramStop> stops = new ArrayList<>();

            if (elements.isArray()) {
                for (JsonNode node : elements) {
                    JsonNode tags = node.path("tags");

                    String name = tags.path("name").asText("Unknown");

                    /*
                     * railway tiene prioridad para estaciones y entradas.
                     * highway identifica las paradas de autobús.
                     * public_transport sirve como fallback.
                     */
                    String type;

                    if (tags.has("railway")) {
                        type = tags.path("railway").asText();
                    } else if (tags.has("highway")) {
                        type = tags.path("highway").asText();
                    } else {
                        type = tags.path("public_transport")
                                .asText("unknown");
                    }

                    String wheelchair =
                            tags.path("wheelchair").asText("unknown");

                    String ref = tags.path("ref").asText(null);
                    String website = tags.path("website").asText(null);

                    double lat = node.path("lat").asDouble();
                    double lon = node.path("lon").asDouble();

                    stops.add(new TelegramStop(
                            name,
                            type,
                            lat,
                            lon,
                            wheelchair,
                            ref,
                            website));
                }
            }

            log.info("Stops obtenidos: {}", stops.size());

            return stops;

        } catch (HttpStatusCodeException e) {
            log.error(
                    "Error HTTP al consultar Overpass. Estado: {}. Respuesta: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e);

            return List.of();

        } catch (Exception e) {
            log.error("Error fetching transport stops", e);
            return List.of();
        }
    }

    public String buildTransportMessage(List<TelegramStop> stops) {

        StringBuilder sb = new StringBuilder();

        sb.append("🚌🚆 *Transportes cercanos*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        List<TelegramStop> metroStations = stops.stream()
                .filter(s ->
                        s.type().equalsIgnoreCase("subway_entrance")
                                || s.type().equalsIgnoreCase("station")
                                || s.type().equalsIgnoreCase("halt")
                                || s.type().equalsIgnoreCase("tram_stop"))
                .toList();

        List<TelegramStop> busStops = stops.stream()
                .filter(s -> s.type().equalsIgnoreCase("bus_stop"))
                .toList();

        for (TelegramStop s : metroStations) {
            sb.append("🚆 *")
                    .append(UtilString.escapeMarkdown(s.displayName()))
                    .append("*\n");

            sb.append("📍 Lat: ")
                    .append(s.lat())
                    .append(", Lon: ")
                    .append(s.lon())
                    .append("\n");

            sb.append("♿ Accesible: ")
                    .append(getAccessibleText(s.accessibility()))
                    .append("\n");

            if (s.ref() != null) {
                sb.append("🔖 Ref: ")
                        .append(s.ref())
                        .append("\n");
            }

            if (s.website() != null) {
                sb.append("🌐 ")
                        .append(s.website())
                        .append("\n");
            }

            sb.append("\n");
        }

        int busesToShow = Math.min(busStops.size(), 5);

        for (int i = 0; i < busesToShow; i++) {
            TelegramStop s = busStops.get(i);

            sb.append("🚌 *")
                    .append(UtilString.escapeMarkdown(s.displayName()))
                    .append("*\n");

            sb.append("📍 Lat: ")
                    .append(s.lat())
                    .append(", Lon: ")
                    .append(s.lon())
                    .append("\n");

            sb.append("♿ Accesible: ")
                    .append(getAccessibleText(s.accessibility()))
                    .append("\n");

            if (s.ref() != null) {
                sb.append("🔖 Ref: ")
                        .append(s.ref())
                        .append("\n");
            }

            if (s.website() != null) {
                sb.append("🌐 ")
                        .append(s.website())
                        .append("\n");
            }

            sb.append("\n");
        }

        if (busStops.size() > 5) {
            sb.append("…y ")
                    .append(busStops.size() - 5)
                    .append(" paradas de bus más cercanas\n");
        }

        return sb.toString();
    }

    private static String getAccessibleText(String accessibility) {
        return "yes".equalsIgnoreCase(accessibility)
                ? "sí"
                : "no";
    }
}