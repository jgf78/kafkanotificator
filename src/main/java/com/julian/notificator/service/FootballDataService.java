package com.julian.notificator.service;

import com.julian.notificator.model.football.FootballData;
import com.julian.notificator.model.football.LiveMatchResponse;

public interface FootballDataService {

    LiveMatchResponse getLiveStatus();

    String formatLiveMatchMessage(FootballData response);

}
