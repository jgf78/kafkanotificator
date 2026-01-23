package com.julian.notificator.model.football;

import lombok.Data;

@Data
public class LiveMatchResponse {

    private boolean playing;
    private String message;
    private FootballData data;
}