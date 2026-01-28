package com.julian.notificator.service;

import com.julian.notificator.model.football.LiveMatchResponse;

public interface FootballDataService {

    LiveMatchResponse getLiveStatus();

    String getNextMatch();

    String formatLiveMatchMessage();

    String getFinishedMatch();

}
