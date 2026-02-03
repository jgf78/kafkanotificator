package com.julian.notificator.model.series;

import java.util.List;

public record TopSeries(
        int position,
        String title,
        int year,
        int rating,
        List<String> genres,
        String platform
    ) {}

