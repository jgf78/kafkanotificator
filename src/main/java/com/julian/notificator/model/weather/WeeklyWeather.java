package com.julian.notificator.model.weather;

import java.util.List;

import lombok.Data;

@Data
public class WeeklyWeather {
    private String city;
    private List<DailyWeather> days;
}
