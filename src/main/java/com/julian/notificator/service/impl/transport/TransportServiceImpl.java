package com.julian.notificator.service.impl.transport;

import java.util.ArrayList;
import java.util.List;

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

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TransportServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super();
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    //@Cacheable(value = "transportStops", key = "#latitude + '-' + #longitude + '-' + #radius")
    public List<TelegramStop> getStopsNearby(String latitude, String longitude, int radius) {
        
        log.info("Buscando transportes cercanos a latitud: {}, longitud:{} metros", latitude, longitude);

        try {

            String query = String.format("""
                [out:json][timeout:25];
                (
                  node(around:%d,%s,%s)[public_transport=platform];
                  node(around:%d,%s,%s)[railway=subway_entrance];
                  node(around:%d,%s,%s)[highway=bus_stop];
                );
                out body;
                """, radius, latitude, longitude,
                     radius, latitude, longitude,
                     radius, latitude, longitude);

            String response = restTemplate.postForObject(
                    OVERPASS_URL,
                    query,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response);
            JsonNode elements = root.get("elements");

            List<TelegramStop> stops = new ArrayList<>();

            if (elements != null && elements.isArray()) {

                for (JsonNode node : elements) {

                    String name = node.path("tags").path("name").asText("Unknown");
                    String type = node.path("tags").path("railway")
                            .asText(node.path("tags").path("highway").asText("unknown"));

                    String wheelchair = node.path("tags").path("wheelchair").asText("unknown");
                    String ref = node.path("tags").path("ref").asText(null);
                    String website = node.path("tags").path("website").asText(null);

                    stops.add(new TelegramStop(
                            name,
                            type,
                            node.path("lat").asDouble(),
                            node.path("lon").asDouble(),
                            wheelchair,
                            ref,
                            website
                    ));
                }
            }

            log.info("Stops obtenidos: {}", stops.size());
            return stops;

        } catch (Exception e) {
            log.error("Error fetching transport stops", e);
            return List.of();
        }
    }
    
    @Override
    public String buildTransportMessage(List<TelegramStop> stops) {

        StringBuilder sb = new StringBuilder();

        sb.append("🚏 *Transportes cercanos*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        if (stops == null || stops.isEmpty()) {
            sb.append("🚫 No se encontraron paradas cerca.");
            return sb.toString();
        }

        stops.stream()
                .limit(10)
                .forEach(stop -> {

                    sb.append(getEmoji(stop.type()))
                      .append(" *")
                      .append(UtilString.escapeMarkdown(stop.displayName()))
                      .append("*\n");

                    if (stop.ref() != null) {
                        sb.append("📍 Parada: ")
                          .append(UtilString.escapeMarkdown(stop.ref()))
                          .append("\n");
                    }

                    sb.append("♿ Accesible: ")
                      .append("yes".equalsIgnoreCase(stop.accessibility()) ? "♿" : "❌")
                      .append("\n");

                    sb.append("📌 Ubicación: ")
                      .append(stop.lat())
                      .append(", ")
                      .append(stop.lon())
                      .append("\n\n");
                });

        sb.append("ℹ️ Mostrando las paradas más cercanas.");

        return sb.toString();
    }
    
    private static String getEmoji(String type) {

        return switch (type) {
            case "bus_stop" -> "🚌";
            case "subway_entrance" -> "🚇";
            case "station" -> "🚆";
            case "tram_stop" -> "🚊";
            default -> "🚏";
        };
    }
}