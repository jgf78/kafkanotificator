package com.julian.notificator.model.cinema;

import java.util.List;

public record TmdbNowPlayingResponse(
        List<TmdbMovie> results
) {}