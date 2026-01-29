package com.julian.notificator.model.weather;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Forecast {

    private List<ForecastItem> list;

    public List<ForecastItem> getList() {
        return list;
    }

    public void setList(List<ForecastItem> list) {
        this.list = list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastItem {
        private long dt; // timestamp en segundos
        private Main main;
        private List<Weather> weather;

        public long getDt() { return dt; }
        public void setDt(long dt) { this.dt = dt; }

        public Main getMain() { return main; }
        public void setMain(Main main) { this.main = main; }

        public List<Weather> getWeather() { return weather; }
        public void setWeather(List<Weather> weather) { this.weather = weather; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        private double temp_min;
        private double temp_max;

        public double getTempMin() { return temp_min; }
        public void setTempMin(double temp_min) { this.temp_min = temp_min; }

        public double getTempMax() { return temp_max; }
        public void setTempMax(double temp_max) { this.temp_max = temp_max; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        private String description;
        private String icon;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}
