package com.julian.notificator.service.impl.mqtt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MqttConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService mqttService;

    public MqttConsumerServiceImpl(@Qualifier("mqttServiceImpl") NotificationService notificationService) {
        this.mqttService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.mqtt}", groupId = "${kafka.group-id}")
    public void consume(MessageRequest request) {
        try {
            log.debug("📥 MqttConsumer - mensaje recibido: {}", request.getMessage());
            mqttService.sendMessage(request.getMessage());
        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }
}

