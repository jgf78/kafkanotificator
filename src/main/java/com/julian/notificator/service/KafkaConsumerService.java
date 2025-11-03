package com.julian.notificator.service;

public interface KafkaConsumerService {
    void consume(String message);
}
