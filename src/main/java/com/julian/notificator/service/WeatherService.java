package com.julian.notificator.service;

import com.julian.notificator.model.weather.WeeklyWeather;

public interface WeatherService {
    
    WeeklyWeather getWeeklyForecast(String city);

    String formatWeeklyWeather(WeeklyWeather weather);
}

