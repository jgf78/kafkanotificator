package com.julian.notificator.model.football;

import lombok.Data;

@Data
public class Score {

    private String winner;
    private String duration;

    private Result fullTime;
    private Result halfTime;
}

