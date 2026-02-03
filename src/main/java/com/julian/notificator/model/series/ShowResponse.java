package com.julian.notificator.model.series;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowResponse {

    private String id;
    private String title;
    private Integer firstAirYear;
    private Integer rating;
    private List<Genre> genres;

}

