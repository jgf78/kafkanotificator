package com.julian.notificator.model.football;

import lombok.Data;

@Data
public class Season {

    private Integer id;
    private String startDate;
    private String endDate;
    private Integer currentMatchday;
    private String winner;
}