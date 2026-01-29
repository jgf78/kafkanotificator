package com.julian.notificator.service.impl.weather;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.weather.DailyWeather;
import com.julian.notificator.model.weather.Forecast;
import com.julian.notificator.model.weather.GeoResponse;
import com.julian.notificator.model.weather.WeeklyWeather;
import com.julian.notificator.service.WeatherService;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String geoUrl;
    private final String apiKey;

    public WeatherServiceImpl(
            RestTemplate restTemplate,
            @Value("${weather.base-url}") String baseUrl,
            @Value("${weather.geo-url}") String geoUrl,
            @Value("${weather.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.geoUrl = geoUrl;
        this.apiKey = apiKey;
    }

    @Override
    public WeeklyWeather getWeeklyForecast(String city) {

        // 🌍 Geocoding
        String geoEndpoint = String.format(
                "%s?q=%s&limit=1&appid=%s",
                geoUrl,
                city,
                apiKey
        );
        GeoResponse[] geo = restTemplate.getForObject(geoEndpoint, GeoResponse[].class);
        if (geo == null || geo.length == 0) {
            throw new IllegalArgumentException("Ciudad no encontrada");
        }

        // 🌦️ Forecast 5 días / 3h
        String weatherEndpoint = String.format(
                "%s?q=%s&units=metric&lang=es&appid=%s",
                baseUrl,
                city,
                apiKey
        );

        Forecast response = restTemplate.getForObject(weatherEndpoint, Forecast.class);
        if (response == null || response.getList() == null) {
            throw new IllegalStateException("No se pudo obtener la previsión");
        }

        // Agrupamos por fecha
        Map<LocalDate, List<Forecast.ForecastItem>> grouped = response.getList().stream()
                .collect(Collectors.groupingBy(
                        f -> Instant.ofEpochSecond(f.getDt()).atZone(ZONE_ID).toLocalDate(),
                        TreeMap::new, Collectors.toList()
                ));

        List<DailyWeather> days = grouped.entrySet().stream()
                .limit(7)
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Forecast.ForecastItem> items = entry.getValue();

                    double min = items.stream().mapToDouble(i -> i.getMain().getTempMin()).min().orElse(0);
                    double max = items.stream().mapToDouble(i -> i.getMain().getTempMax()).max().orElse(0);

                    // Icono más frecuente
                    String icon = items.stream()
                            .map(i -> i.getWeather().get(0).getIcon())
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("01d");

                    String description = items.stream()
                            .map(i -> i.getWeather().get(0).getDescription())
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("");

                    return new DailyWeather(date, description, min, max, weatherEmoji(icon));
                })
                .toList();

        WeeklyWeather weekly = new WeeklyWeather();
        weekly.setCity(city);
        weekly.setDays(days);

        return weekly;
    }

    @Override
    public String formatWeeklyWeather(WeeklyWeather weather) {
        StringBuilder msg = new StringBuilder();
        msg.append("📍 Tiempo en *")
           .append(capitalize(weather.getCity()))
           .append("* (7 días)\n\n");

        for (DailyWeather day : weather.getDays()) {
            String dayName = day.getDate().getDayOfWeek()
                    .getDisplayName(java.time.format.TextStyle.SHORT, Locale.forLanguageTag("es"))
                    .toUpperCase();

            msg.append("📅 ").append(dayName).append(" ").append(day.getEmoji()).append("\n")
               .append("🌡️ ").append(Math.round(day.getMinTemp())).append("º / ")
               .append(Math.round(day.getMaxTemp())).append("º\n")
               .append("📝 ").append(capitalize(day.getDescription())).append("\n\n");
        }

        return msg.toString();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private String weatherEmoji(String icon) {
        return switch (icon) {
            case "01d", "01n" -> "☀️";
            case "02d", "02n" -> "🌤️";
            case "03d", "03n", "04d", "04n" -> "☁️";
            case "09d", "09n", "10d", "10n" -> "🌧️";
            case "11d", "11n" -> "⛈️";
            case "13d", "13n" -> "❄️";
            case "50d", "50n" -> "🌫️";
            default -> "🌡️";
        };
    }
}
