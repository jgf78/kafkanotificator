package com.julian.notificator.service.impl.telegram;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.model.telegram.TelegramPollRequest;
import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;
import com.julian.notificator.service.SubscriberService;
import com.julian.notificator.service.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService telegramService;
    private final SubscriberService subscriberService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public TelegramConsumerServiceImpl(
            @Qualifier("telegramServiceImpl") NotificationService telegramService,
            SubscriberService subscriberService) {
        this.telegramService = telegramService;
        this.subscriberService = subscriberService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.telegram}", groupId = "${kafka.group-id}")
    public void consume(String messageOrJson) {
        try {
            var telegramPoll = tryParse(messageOrJson, TelegramPollRequest.class);
            if (telegramPoll != null && telegramPoll.getQuestion() != null
                    && telegramPoll.getOptions() != null && !telegramPoll.getOptions().isEmpty()) {
                telegramService.sendPoll(telegramPoll);
                subscriberService.notifyAllSubscribers(Constants.TELEGRAM_POLL_EVENT, telegramPoll);
                log.debug("📥 TelegramConsumer - encuesta procesada: {}", telegramPoll.getQuestion());
                return;
            }

            var payload = tryParse(messageOrJson, MessagePayload.class);
            if (payload == null) {
                payload = new MessagePayload();
                payload.setMessage(messageOrJson);
            }

            if (payload.getFile() != null && !payload.getFile().isBlank()) {
                telegramService.sendMessageFile(payload, DestinationTelegramType.ALL);
            } else if (payload.isPin()) {
                telegramService.sendPinMessage(payload.getMessage());
                subscriberService.notifyAllSubscribers(Constants.TELEGRAM_TEXT_PIN_EVENT, payload.getMessage());
            } else {
                String message = payload.getMessage();

                if (telegramService instanceof TelegramServiceImpl telegramImpl) {

                    String matchKey = telegramImpl.extractMatchKey(message);

                    if (matchKey != null) {
                        telegramImpl.sendOrUpdateMatchMessage(matchKey, message);
                    } else {
                        telegramImpl.sendMessage(message, DestinationTelegramType.ALL);
                    }

                } else {
                    telegramService.sendMessage(message, DestinationTelegramType.ALL);
                }

                subscriberService.notifyAllSubscribers(Constants.TELEGRAM_TEXT_EVENT, message);
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
