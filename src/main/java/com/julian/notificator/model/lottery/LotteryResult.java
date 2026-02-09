package com.julian.notificator.model.lottery;

import java.util.List;


public record LotteryResult(
        String id,
        Game game,
        String drawId,
        String drawDate,
        String dayOfWeek,
        int year,
        String status,
        String jackpot,
        String jackpotFormatted,
        List<Integer> combination,
        ResultData resultData,
        Statistics statistics
) {}
