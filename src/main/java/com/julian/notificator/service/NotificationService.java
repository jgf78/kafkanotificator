package com.julian.notificator.service;

import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.model.telegram.TelegramPollRequest;

public interface NotificationService {
    
    String getChannelName();
    
    void sendPinMessage(String message);
    
    void sendPoll(TelegramPollRequest telegramPoll);

    void sendMessage(String message);
    
    void sendMessage(String message, DestinationTelegramType destination);

    void sendMessageFile(MessageRequest messageRequest, DestinationTelegramType destination);
    
}