package com.julian.notificator.model.weather;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyWeather {
    
    private LocalDate date;
    
    private String description;
    
    private double minTemp;
    
    private double maxTemp;
    
    private String emoji;
    
}


