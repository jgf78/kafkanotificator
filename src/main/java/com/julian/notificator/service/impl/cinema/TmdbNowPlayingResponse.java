package com.julian.notificator.service.impl.cinema;

import java.util.List;

import com.julian.notificator.model.cinema.TmdbMovie;

public record TmdbNowPlayingResponse(
        List<TmdbMovie> results
) {}