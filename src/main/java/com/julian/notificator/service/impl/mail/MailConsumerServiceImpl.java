package com.julian.notificator.service.impl.mail;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService mailService;

    public MailConsumerServiceImpl(@Qualifier("mailServiceImpl") NotificationService notificationService) {
        this.mailService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.mail}", groupId = "${kafka.group-id}")
    public void consume(String message) {
        try {
            log.debug("📥 MailConsumer - mensaje recibido: {}", message);
            mailService.sendMessage(message);
        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }
}

