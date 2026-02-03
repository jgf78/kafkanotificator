package com.julian.notificator.model.series;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Genre {

    private String id;
    private String name;

}

