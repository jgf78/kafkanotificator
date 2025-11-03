package com.julian.notificator.service;

public interface KafkaProducerService {
    void sendMessage(String message);
}
