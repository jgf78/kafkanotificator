package com.julian.notificator.service.impl.telegram;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TelegramConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService telegramService;

    public TelegramConsumerServiceImpl(@Qualifier("telegramServiceImpl") NotificationService notificationService) {
        this.telegramService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.telegram}", groupId = "${kafka.group-id}")
    public void consume(String message) {
        try {
            log.debug("📥 TelegramConsumer - mensaje recibido: {}", message);
            telegramService.sendMessage(message);
        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }
}

