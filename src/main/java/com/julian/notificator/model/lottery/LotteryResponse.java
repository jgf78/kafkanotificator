package com.julian.notificator.model.lottery;

import java.util.List;



public record LotteryResponse(
        boolean success,
        List<LotteryResult> data,
        Meta meta,
        String timestamp
) {}

