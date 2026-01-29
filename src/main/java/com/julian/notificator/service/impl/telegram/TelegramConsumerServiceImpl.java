package com.julian.notificator.service.impl.telegram;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService telegramService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramConsumerServiceImpl(@Qualifier("telegramServiceImpl") NotificationService notificationService) {
        this.telegramService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.telegram}", groupId = "${kafka.group-id}")
    public void consume(String messageOrJson) {
        try {
            MessagePayload payload;
            boolean isJson = false;

            try {
                payload = objectMapper.readValue(messageOrJson, MessagePayload.class);
                isJson = true;
            } catch (Exception e) {
                payload = new MessagePayload();
                payload.setMessage(messageOrJson);
            }

            if (isJson && payload.getFile() != null && !payload.getFile().isBlank()) {
                telegramService.sendMessageFile(payload);
            } else if (payload.isPin()) { 
                telegramService.sendPinMessage(payload.getMessage());
            } else {
                telegramService.sendMessage(payload.getMessage());
            }

            log.debug("📥 TelegramConsumer - mensaje procesado: {}", payload.getMessage());
        } catch (Exception e) {
            log.error("❌ Error procesando mensaje Telegram: {}", e.getMessage(), e);
        }
    }
}
