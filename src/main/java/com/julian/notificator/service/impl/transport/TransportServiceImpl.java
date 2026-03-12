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
    
    public String buildTransportMessage(List<TelegramStop> stops) {

        StringBuilder sb = new StringBuilder();
        sb.append("🚌🚆 *Transportes cercanos*\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        // Filtrar estaciones de metro/cercanías y buses
        List<TelegramStop> metroStations = stops.stream()
                .filter(s -> s.type().equalsIgnoreCase("subway_entrance")
                          || s.type().equalsIgnoreCase("railway")
                          || s.type().equalsIgnoreCase("tram_stop"))
                .toList();

        List<TelegramStop> busStops = stops.stream()
                .filter(s -> s.type().equalsIgnoreCase("bus_stop"))
                .toList();

        // Mostrar primero metro/cercanías
        for (TelegramStop s : metroStations) {
            sb.append("🚆 *").append(UtilString.escapeMarkdown(s.displayName())).append("*\n");
            sb.append("📍 Lat: ").append(s.lat()).append(", Lon: ").append(s.lon()).append("\n");
            sb.append("♿ Accesible: ").append(getAccessibleText(s.accessibility())).append("\n");
            if (s.ref() != null) sb.append("🔖 Ref: ").append(s.ref()).append("\n");
            if (s.website() != null) sb.append("🌐 ").append(s.website()).append("\n");
            sb.append("\n");
        }

        // Luego hasta 5 buses
        int busesToShow = Math.min(busStops.size(), 5);
        for (int i = 0; i < busesToShow; i++) {
            TelegramStop s = busStops.get(i);
            sb.append("🚌 *").append(UtilString.escapeMarkdown(s.displayName())).append("*\n");
            sb.append("📍 Lat: ").append(s.lat()).append(", Lon: ").append(s.lon()).append("\n");
            sb.append("♿ Accesible: ").append(getAccessibleText(s.accessibility())).append("\n");
            if (s.ref() != null) sb.append("🔖 Ref: ").append(s.ref()).append("\n");
            if (s.website() != null) sb.append("🌐 ").append(s.website()).append("\n");
            sb.append("\n");
        }

        // Aviso si hay más buses que no se muestran
        if (busStops.size() > 5) {
            sb.append("…y ").append(busStops.size() - 5).append(" paradas de bus más cercanas\n");
        }

        return sb.toString();
    }
    
    private static String getAccessibleText(String accessibility) {
        return "yes".equalsIgnoreCase(accessibility) ? "sí" : "no";
    }
    
}