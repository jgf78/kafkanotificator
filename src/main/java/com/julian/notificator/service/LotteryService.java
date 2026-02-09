package com.julian.notificator.service;

import java.io.IOException;

import com.julian.notificator.model.lottery.LotteryResponse;
import com.rometools.rome.io.FeedException;

public interface LotteryService {
    
    LotteryResponse getLatestResults();

    String buildLotteryMessage(LotteryResponse latestResults) throws IllegalArgumentException, FeedException, IOException; 
}

