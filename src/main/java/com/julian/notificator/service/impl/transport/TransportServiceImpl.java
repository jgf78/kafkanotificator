package com.julian.notificator.service.impl.transport;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
            /*
             * Misma consulta que funciona desde Postman/curl.
             *
             * Ejemplo:
             *
             * [out:json][timeout:25];
             * (
             *   node(around:500,40.346467,-3.82464)["highway"="bus_stop"];
             *   node(around:500,40.34714,-3.82481)["railway"~"station|halt|tram_stop|subway_entrance"];
             *   node(around:500,40.34714,-3.82481)["public_transport"="platform"];
             * );
             * out body;
             */

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

            /*
             * Codificamos manualmente el parámetro "data", igual que hace
             * curl --data-urlencode.
             */
            String encodedQuery = URLEncoder.encode(
                    query,
                    StandardCharsets.UTF_8);

            String body = "data=" + encodedQuery;

            log.debug("Body enviado a Overpass: {}", body);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_FORM_URLENCODED);

            headers.setAccept(
                    Collections.singletonList(
                            MediaType.APPLICATION_JSON));

            headers.set(
                    HttpHeaders.USER_AGENT,
                    USER_AGENT);

            HttpEntity<String> request =
                    new HttpEntity<>(body, headers);

            log.info(
                    "Enviando petición POST a Overpass: {}",
                    OVERPASS_URL);

            ResponseEntity<String> httpResponse =
                    restTemplate.exchange(
                            OVERPASS_URL,
                            HttpMethod.POST,
                            request,
                            String.class);

            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.warn(
                        "Overpass ha devuelto el estado HTTP: {}",
                        httpResponse.getStatusCode());

                return List.of();
            }

            String response = httpResponse.getBody();

            if (response == null || response.isBlank()) {
                log.warn(
                        "Overpass ha devuelto una respuesta vacía");

                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode elements = root.path("elements");

            List<TelegramStop> stops = new ArrayList<>();

            if (elements.isArray()) {

                for (JsonNode node : elements) {

                    JsonNode tags = node.path("tags");

                    String name =
                            tags.path("name")
                                    .asText("Unknown");

                    /*
                     * railway tiene prioridad para estaciones/entradas.
                     * highway identifica las paradas de autobús.
                     * public_transport sirve como fallback.
                     */
                    String type;

                    if (tags.has("railway")) {

                        type = tags.path("railway")
                                .asText();

                    } else if (tags.has("highway")) {

                        type = tags.path("highway")
                                .asText();

                    } else {

                        type = tags.path("public_transport")
                                .asText("unknown");
                    }

                    String wheelchair =
                            tags.path("wheelchair")
                                    .asText("unknown");

                    String ref =
                            tags.path("ref")
                                    .asText(null);

                    String website =
                            tags.path("website")
                                    .asText(null);

                    double lat =
                            node.path("lat")
                                    .asDouble();

                    double lon =
                            node.path("lon")
                                    .asDouble();

                    stops.add(
                            new TelegramStop(
                                    name,
                                    type,
                                    lat,
                                    lon,
                                    wheelchair,
                                    ref,
                                    website));
                }
            }

            log.info(
                    "Stops obtenidos: {}",
                    stops.size());

            return stops;

        } catch (Exception e) {

            log.error(
                    "Error fetching transport stops",
                    e);

            return List.of();
        }
    }

    public String buildTransportMessage(
            List<TelegramStop> stops) {

        StringBuilder sb =
                new StringBuilder();

        sb.append("🚌🚆 *Transportes cercanos*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        List<TelegramStop> metroStations =
                stops.stream()
                        .filter(s ->
                                s.type().equalsIgnoreCase(
                                        "subway_entrance")
                                        || s.type().equalsIgnoreCase(
                                                "station")
                                        || s.type().equalsIgnoreCase(
                                                "halt")
                                        || s.type().equalsIgnoreCase(
                                                "tram_stop"))
                        .toList();

        List<TelegramStop> busStops =
                stops.stream()
                        .filter(s ->
                                s.type().equalsIgnoreCase(
                                        "bus_stop"))
                        .toList();

        for (TelegramStop s : metroStations) {

            sb.append("🚆 *")
                    .append(
                            UtilString.escapeMarkdown(
                                    s.displayName()))
                    .append("*\n");

            sb.append("📍 Lat: ")
                    .append(s.lat())
                    .append(", Lon: ")
                    .append(s.lon())
                    .append("\n");

            sb.append("♿ Accesible: ")
                    .append(
                            getAccessibleText(
                                    s.accessibility()))
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

        int busesToShow =
                Math.min(
                        busStops.size(),
                        5);

        for (int i = 0;
             i < busesToShow;
             i++) {

            TelegramStop s =
                    busStops.get(i);

            sb.append("🚌 *")
                    .append(
                            UtilString.escapeMarkdown(
                                    s.displayName()))
                    .append("*\n");

            sb.append("📍 Lat: ")
                    .append(s.lat())
                    .append(", Lon: ")
                    .append(s.lon())
                    .append("\n");

            sb.append("♿ Accesible: ")
                    .append(
                            getAccessibleText(
                                    s.accessibility()))
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
                    .append(
                            busStops.size() - 5)
                    .append(
                            " paradas de bus más cercanas\n");
        }

        return sb.toString();
    }

    private static String getAccessibleText(
            String accessibility) {

        return "yes".equalsIgnoreCase(
                accessibility)
                ? "sí"
                : "no";
    }
}