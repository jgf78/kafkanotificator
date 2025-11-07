package com.julian.notificator.service;

import com.julian.notificator.model.DestinationType;

public interface KafkaProducerService {
    void sendMessage(String message, DestinationType destination);
}
