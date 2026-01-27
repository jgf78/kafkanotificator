package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.model.cinema.TmdbMovie;

public interface CinemaDataService {

    String buildCarteleraMessage(List<TmdbMovie> movies);

    List<TmdbMovie> getTop10NowPlaying();
}
