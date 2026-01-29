package com.julian.notificator.service;

import com.julian.notificator.model.MessagePayload;

public interface NotificationService {
    void sendMessage(String message);
    String getChannelName();
    void sendMessageFile(MessagePayload payload);
    void sendPinMessage(String message);
}