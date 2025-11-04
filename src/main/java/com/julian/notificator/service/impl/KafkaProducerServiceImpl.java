package com.julian.notificator.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.KafkaProducerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    @Value("${kafka.topics.discord}")
    private String discord;
    
    @Value("${kafka.topics.telegram}")
    private String telegram;
    
    @Value("${kafka.topics.mail}")
    private String mail;
    
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(String message) {
        kafkaTemplate.send(discord, message);
        kafkaTemplate.send(telegram, message);
        kafkaTemplate.send(mail, message);
        log.debug("KafkaProducerService - sendMessage, message: {}", message);
    }
}
