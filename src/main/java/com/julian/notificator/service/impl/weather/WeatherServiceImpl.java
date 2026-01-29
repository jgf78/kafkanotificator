package com.julian.notificator.service.impl.weather;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.weather.DailyWeather;
import com.julian.notificator.model.weather.GeoResponse;
import com.julian.notificator.model.weather.WeatherResponse;
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

        double lat = geo[0].getLat();
        double lon = geo[0].getLon();

        // 🌦️ Forecast semanal
        String weatherEndpoint = String.format(
                "%s?q=%s&units=metric&lang=es&appid=%s",
                baseUrl,
                city,
                apiKey
            );

        WeatherResponse response = restTemplate.getForObject(weatherEndpoint, WeatherResponse.class);

        if (response == null || response.getDaily() == null) {
            throw new IllegalStateException("No se pudo obtener la previsión semanal");
        }

        List<DailyWeather> days = response.getDaily().stream()
                .limit(7)
                .map(d -> new DailyWeather(
                        Instant.ofEpochSecond(d.getDt())
                                .atZone(ZONE_ID)
                                .toLocalDate(),
                        d.getWeather().get(0).getDescription(),
                        d.getTemp().getMin(),
                        d.getTemp().getMax(),
                        weatherEmoji(d.getWeather().get(0).getIcon())
                ))
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

            String dayName = day.getDate()
                    .getDayOfWeek()
                    .getDisplayName(java.time.format.TextStyle.SHORT, Locale.forLanguageTag("es"))
                    .toUpperCase();

            msg.append("📅 ")
               .append(dayName)
               .append(" ")
               .append(day.getEmoji())
               .append("\n");

            msg.append("🌡️ ")
               .append(Math.round(day.getMinTemp()))
               .append("º / ")
               .append(Math.round(day.getMaxTemp()))
               .append("º\n");

            msg.append("📝 ")
               .append(capitalize(day.getDescription()))
               .append("\n\n");
        }

        return msg.toString();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
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