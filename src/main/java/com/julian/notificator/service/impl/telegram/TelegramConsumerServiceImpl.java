package com.julian.notificator.service.impl.telegram;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.telegram.TelegramPollRequest;
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
            var telegramPoll = tryParse(messageOrJson, TelegramPollRequest.class);
            if (telegramPoll != null && telegramPoll.getQuestion() != null
                    && telegramPoll.getOptions() != null && !telegramPoll.getOptions().isEmpty()) {
                telegramService.sendPoll(telegramPoll);
                log.debug("📥 TelegramConsumer - encuesta procesada: {}", telegramPoll.getQuestion());
                return;
            }

            var payload = tryParse(messageOrJson, MessagePayload.class);
            if (payload == null) {
                payload = new MessagePayload();
                payload.setMessage(messageOrJson);
            }

            if (payload.getFile() != null && !payload.getFile().isBlank()) {
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

    private <T> T tryParse(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception ignored) {
            return null;
        }
    }

}
