package com.julian.notificator.service.impl.discord;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiscordConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService discordService;

    public DiscordConsumerServiceImpl(@Qualifier("discordServiceImpl") NotificationService notificationService) {
        this.discordService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.discord}", groupId = "${kafka.group-id}")
    public void consume(String message) {
        try {
            log.debug("📥 DiscordConsumer - mensaje recibido: {}", message);
            discordService.sendMessage(message);
        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }
}

