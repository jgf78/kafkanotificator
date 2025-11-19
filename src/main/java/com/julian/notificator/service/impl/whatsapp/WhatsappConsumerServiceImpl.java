package com.julian.notificator.service.impl.whatsapp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WhatsappConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService whatsappService;

    public WhatsappConsumerServiceImpl(@Qualifier("whatsappServiceImpl") NotificationService notificationService) {
        this.whatsappService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.whatsapp}", groupId = "${kafka.group-id}")
    public void consume(String message) {
        try {
            log.debug("📥 WhatsappConsumer - mensaje recibido: {}", message);
            whatsappService.sendMessage(message);
        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }
}

