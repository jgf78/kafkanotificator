package com.julian.notificator.service;

public interface NotificationService {
    void sendMessage(String message);
    String getChannelName();
}