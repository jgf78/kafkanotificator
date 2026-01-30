package com.julian.notificator.service;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.telegram.TelegramPollRequest;

public interface NotificationService {
    
    void sendMessage(String message);
    
    String getChannelName();
    
    void sendMessageFile(MessagePayload payload);
    
    void sendPinMessage(String message);
    
    void sendPoll(TelegramPollRequest telegramPoll);
    
}