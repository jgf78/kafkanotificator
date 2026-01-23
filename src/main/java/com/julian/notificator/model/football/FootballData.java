package com.julian.notificator.model.football;

import java.util.List;
import lombok.Data;

@Data
public class FootballData {

    private Filters filters;
    private ResultSet resultSet;
    private List<Match> matches;
}
