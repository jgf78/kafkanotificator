package com.julian.notificator.service;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.model.telegram.TelegramPollRequest;

public interface NotificationService {
    
    String getChannelName();
    
    void sendPinMessage(String message);
    
    void sendPoll(TelegramPollRequest telegramPoll);

    void sendMessageFile(MessagePayload payload);
    
    void sendMessageFile(MessagePayload payload, DestinationTelegramType destination);

    void sendMessage(String message);
    
    void sendMessage(String message, DestinationTelegramType destination);
    
}