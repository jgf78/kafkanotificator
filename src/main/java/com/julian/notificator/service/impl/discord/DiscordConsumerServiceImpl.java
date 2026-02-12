package com.julian.notificator.service.impl.discord;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiscordConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService discordService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DiscordConsumerServiceImpl(@Qualifier("discordServiceImpl") NotificationService notificationService) {
        this.discordService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.discord}", groupId = "${kafka.group-id}")
    public void consume(String messageOrJson) {
        try {
            log.debug("📥 DiscordConsumer - mensaje recibido: {}", messageOrJson);
            
            var payload = tryParse(messageOrJson, MessagePayload.class);
            if (payload == null) {
                payload = new MessagePayload();
                payload.setMessage(messageOrJson);
            }

            if (payload.getFile() != null && !payload.getFile().isBlank()) {
                discordService.sendMessageFile(payload);
            } else {
                discordService.sendMessage(payload.getMessage());
            }
            
        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }
    
    private <T> T tryParse(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception ignored) {
            return null;
        }
    }
}

