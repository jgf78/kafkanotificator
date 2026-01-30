package com.julian.notificator.service;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.telegram.TelegramPollRequest;

public abstract class AbstractNotificationService implements NotificationService {

    @Override
    public void sendMessageFile(MessagePayload payload) {
    }

    @Override
    public void sendPinMessage(String message) {
    }
    
    @Override
    public void sendPoll(TelegramPollRequest telegramPoll) {
        
    }

}
