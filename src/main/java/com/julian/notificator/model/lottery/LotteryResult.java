package com.julian.notificator.model.lottery;

import java.util.List;

import org.hibernate.stat.Statistics;
import org.telegram.telegrambots.meta.api.objects.games.Game;

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
