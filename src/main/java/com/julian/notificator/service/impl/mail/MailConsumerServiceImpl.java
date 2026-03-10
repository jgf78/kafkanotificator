package com.julian.notificator.service.impl.mail;

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
public class MailConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService mailService;

    public MailConsumerServiceImpl(@Qualifier("mailServiceImpl") NotificationService notificationService) {
        this.mailService = notificationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.topics.mail}", groupId = "${kafka.group-id}")
    public void consume(MessageRequest request) {

        try {

            String message = request.getMessage();
            MessagePayload payload = request.getMessagePayload();

            log.debug("📥 MailConsumer - mensaje recibido: {}", message);

            // 1️⃣ Envío con archivo
            if (payload != null && payload.getFile() != null && !payload.getFile().isBlank()) {

                mailService.sendMessageFile(request, null);

                log.debug("📎 MailConsumer - archivo enviado: {}", payload.getFilename());

            }
            // 2️⃣ Mensaje normal
            else {

                mailService.sendMessage(message);

                log.debug("✉️ MailConsumer - mensaje enviado");

            }

        } catch (Exception e) {
            log.error("❌ Error al procesar el mensaje Mail: {}", e.getMessage(), e);
        }
    }

}