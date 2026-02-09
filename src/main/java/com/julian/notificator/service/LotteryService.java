package com.julian.notificator.service;

import com.julian.notificator.model.lottery.LotteryResponse;

public interface LotteryService {
    
    LotteryResponse getLatestResults();

    String buildLotteryMessage(LotteryResponse latestResults); 
}

