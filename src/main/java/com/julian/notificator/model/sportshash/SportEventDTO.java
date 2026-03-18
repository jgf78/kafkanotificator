package com.julian.notificator.model.sportshash;

import java.util.List;

public record SportEventDTO(
        String eventTime,
        String sport,
        String competition,
        String matchName,
        List<String> streamUrls
    ) { }
