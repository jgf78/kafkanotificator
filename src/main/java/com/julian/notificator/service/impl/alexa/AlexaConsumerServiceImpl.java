package com.julian.notificator.service.impl.alexa;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlexaConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService alexaService;

    public AlexaConsumerServiceImpl(@Qualifier("alexaServiceImpl") NotificationService notificationService) {
        this.alexaService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.alexa}", groupId = "${kafka.group-id}")
    public void consume(MessageRequest request) {
        try {
            log.debug("📥 AlexaConsumer - mensaje recibido: {}", request.getMessage());
            alexaService.sendMessage(request.getMessage());
        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }
}

