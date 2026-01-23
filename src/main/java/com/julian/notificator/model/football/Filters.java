package com.julian.notificator.model.football;

import java.util.List;

import lombok.Data;

@Data
public class Filters {

    private String competitions;
    private String permission;
    private List<String> status;
    private Integer limit;
}
