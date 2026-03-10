package com.julian.notificator.service.impl.discord;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.service.KafkaConsumerService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiscordConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService discordService;

    public DiscordConsumerServiceImpl(@Qualifier("discordServiceImpl") NotificationService notificationService) {
        this.discordService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.discord}", groupId = "${kafka.group-id}")
    public void consume(MessageRequest request) {

        try {

            String message = request.getMessage();
            MessagePayload payload = request.getMessagePayload();

            log.debug("📥 DiscordConsumer - mensaje recibido: {}", message);

            // 1️⃣ Mensaje con archivo
            if (payload != null && payload.getFile() != null && !payload.getFile().isBlank()) {

                discordService.sendMessageFile(request, null);

                log.debug("📎 DiscordConsumer - archivo enviado: {}", payload.getFilename());

            }
            // 2️⃣ Mensaje normal
            else {

                discordService.sendMessage(message);

                log.debug("💬 DiscordConsumer - mensaje enviado");

            }

        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje Discord: {}", e.getMessage(), e);
        }
    }

}