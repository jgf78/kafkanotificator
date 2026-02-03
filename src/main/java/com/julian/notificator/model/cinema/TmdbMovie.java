package com.julian.notificator.model.cinema;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovie(
        String title,
        String overview,
        @JsonProperty("release_date")
        String releaseDate,
        double popularity
)implements Serializable {}
