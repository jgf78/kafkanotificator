package com.julian.notificator.model.football;

import java.util.List;

import lombok.Data;

@Data
public class Match {

    private Long id;
    private String utcDate;
    private String status;
    private Integer matchday;
    private String stage;
    private String group;
    private String lastUpdated;

    private Area area;
    private Competition competition;
    private Season season;

    private Team homeTeam;
    private Team awayTeam;
    private List<Referee> referees;

    private Score score;
}

