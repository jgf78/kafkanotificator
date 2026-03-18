package com.julian.notificator.service.impl.telegram;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.MessageRequest;
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

    public TelegramConsumerServiceImpl(
            @Qualifier("telegramServiceImpl") NotificationService telegramService,
            SubscriberService subscriberService) {
        this.telegramService = telegramService;
        this.subscriberService = subscriberService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.telegram}", groupId = "${kafka.group-id}")
    public void consume(MessageRequest request) {

        try {

            String message = request.getMessage();
            TelegramPollRequest poll = request.getTelegramPollRequest();
            MessagePayload payload = request.getMessagePayload();

            // 1️⃣ Encuesta
            if (poll != null 
                    && poll.getQuestion() != null 
                    && poll.getOptions() != null 
                    && !poll.getOptions().isEmpty()) {

                telegramService.sendPoll(request);

                subscriberService.notifyAllSubscribers(
                        Constants.TELEGRAM_POLL_EVENT,
                        poll
                );

                log.debug("📥 TelegramConsumer - encuesta procesada: {}", poll.getQuestion());
                return;
            }

            // 2️⃣ Archivo
            if (payload != null && payload.getFile() != null && !payload.getFile().isBlank()) {

                telegramService.sendMessageFile(request, request.getDestinationTelegram());

                log.debug("📥 TelegramConsumer - archivo enviado: {}", payload.getFilename());
                return;
            }

            // 3️⃣ Mensaje anclado
            if (payload != null && payload.isPin()) {

                telegramService.sendPinMessage(message);

                subscriberService.notifyAllSubscribers(
                        Constants.TELEGRAM_TEXT_PIN_EVENT,
                        message
                );

                log.debug("📥 TelegramConsumer - mensaje anclado: {}", message);
                return;
            }

            // 4️⃣ Mensaje normal o actualización de partido
            if (telegramService instanceof TelegramServiceImpl telegramImpl) {

                String matchKey = telegramImpl.extractMatchKey(message);

                if (matchKey != null) {
                    telegramImpl.sendOrUpdateMatchMessage(matchKey, message);
                } else {
                    telegramImpl.sendMessage(message, request.getDestinationTelegram());
                }

            } else {
                telegramService.sendMessage(message, request.getDestinationTelegram());
            }

            subscriberService.notifyAllSubscribers(
                    Constants.TELEGRAM_TEXT_EVENT,
                    message
            );

            log.debug("📥 TelegramConsumer - mensaje procesado: {}", message);

        } catch (Exception e) {
            log.error("❌ Error procesando mensaje Telegram: {}", e.getMessage(), e);
        }
    }

}