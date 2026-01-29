package com.julian.notificator.model.weather;

import java.util.List;

import lombok.Data;

// Clase raíz para /forecast (5 días / 3h)
@Data
public class Forecast {
    private String cod;
    private int cnt;
    private List<ForecastItem> list;
    private City city;

    @Data
    public static class ForecastItem {
        private long dt; // timestamp en segundos
        private Main main;
        private List<Weather> weather;
        private Clouds clouds;
        private Wind wind;
        private int visibility;
        private double pop; // probabilidad de lluvia
        private Sys sys;
        private String dt_txt; // fecha/hora en texto
    }

    @Data
    public static class Main {
        private double temp;
        private double feels_like;
        private double temp_min;
        private double temp_max;
        private int pressure;
        private int sea_level;
        private int grnd_level;
        private int humidity;
        private double temp_kf;

        // Para mapear correctamente el JSON, si es necesario
        public double getTempMin() { return temp_min; }
        public double getTempMax() { return temp_max; }
    }

    @Data
    public static class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;
    }

    @Data
    public static class Clouds {
        private int all;
    }

    @Data
    public static class Wind {
        private double speed;
        private int deg;
        private double gust;
    }

    @Data
    public static class Sys {
        private String pod; // "n" o "d"
    }

    @Data
    public static class City {
        private long id;
        private String name;
        private Coord coord;
        private String country;
        private int population;
        private int timezone;
        private long sunrise;
        private long sunset;
    }

    @Data
    public static class Coord {
        private double lat;
        private double lon;
    }
}
