package com.julian.notificator.model.football;

import lombok.Data;

@Data
public class ResultSet {

    private Integer count;
    private String competitions;
    private String first;
    private String last;
    private Integer played;
    private Integer wins;
    private Integer draws;
    private Integer losses;
}
