package com.julian.notificator.service;

import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.DestinationTelegramType;

public abstract class AbstractNotificationService implements NotificationService {

    @Override
    public void sendMessageFile(MessageRequest messageRequest, DestinationTelegramType destination) {
    }
    
    @Override
    public void sendPinMessage(MessageRequest request) {
    }
    
    @Override
    public void sendPoll(MessageRequest request) {
    }
    
    @Override
    public void sendMessage(String message, DestinationTelegramType destination) {
    }

}
