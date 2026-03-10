package com.julian.notificator.service;

import com.julian.notificator.model.MessageRequest;

public interface KafkaConsumerService {
    void consume(MessageRequest request);
}
