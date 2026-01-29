package com.julian.notificator.service;

import com.julian.notificator.model.MessagePayload;

public abstract class AbstractNotificationService implements NotificationService {

    @Override
    public void sendMessageFile(MessagePayload payload) {
    }

    @Override
    public void sendPinMessage(String message) {
    }

}
