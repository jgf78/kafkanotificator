package com.julian.notificator.model.weather;

import lombok.Data;

@Data
public class GeoResponse {
    private String name;
    private double lat;
    private double lon;
}

