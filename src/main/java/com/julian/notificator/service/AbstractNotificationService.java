package com.julian.notificator.service;

import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.model.telegram.TelegramPollRequest;

public abstract class AbstractNotificationService implements NotificationService {

    @Override
    public void sendMessageFile(MessageRequest messageRequest, DestinationTelegramType destination) {
    }
    
    @Override
    public void sendPinMessage(String message) {
    }
    
    @Override
    public void sendPoll(TelegramPollRequest telegramPoll) {
    }
    
    @Override
    public void sendMessage(String message, DestinationTelegramType destination) {
    }

}
