package com.julian.notificator.model.lottery;

import java.util.List;

public record ResultData(
        Integer complementario,
        Integer reintegro,
        List<Integer> estrellas,
        Joker joker,
        String combinacionRaw
) {}

