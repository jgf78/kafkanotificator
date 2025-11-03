package com.julian.notificator.service.impl;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.DiscordService;
import com.julian.notificator.service.KafkaConsumerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final DiscordService discordService;

    public KafkaConsumerServiceImpl(DiscordService discordService) {
        this.discordService = discordService;
    }

    @Override
    @KafkaListener(topics = "discord-messages", groupId = "discord-group")
    public void consume(String message) {
        log.debug("KafkaConsumerService - consume, 📥 Mensaje recibido de Kafka: : {}", message);
        discordService.sendMessageToDiscord(message);
    }
}
