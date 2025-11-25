package com.julian.notificator.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.KafkaProducerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    @Value("${kafka.topics.discord}")
    private String discord;

    @Value("${kafka.topics.telegram}")
    private String telegram;

    @Value("${kafka.topics.mail}")
    private String mail;

    @Value("${kafka.topics.alexa}")
    private String alexa;

    @Value("${kafka.topics.whatsapp}")
    private String whatsapp;

    @Value("${kafka.topics.mqtt}")
    private String mqtt;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(String message, DestinationType destination) {

        switch (destination) {
        case DISCORD -> kafkaTemplate.send(discord, message);
        case TELEGRAM -> kafkaTemplate.send(telegram, message);
        case MAIL -> kafkaTemplate.send(mail, message);
        case ALEXA -> kafkaTemplate.send(alexa, message);
        case WHATSAPP -> kafkaTemplate.send(whatsapp, message);
        case MQTT -> kafkaTemplate.send(mqtt, message);
        case ALL -> {
            kafkaTemplate.send(discord, message);
            kafkaTemplate.send(telegram, message);
            kafkaTemplate.send(mail, message);
            kafkaTemplate.send(alexa, message);
            kafkaTemplate.send(whatsapp, message);
            kafkaTemplate.send(mqtt, message);
        }
        }
        log.info("KafkaProducerService - sendMessage. Mensaje enviado a {}: {}", destination, message);

    }

    @Override
    public void sendFileToTelegram(String message, MultipartFile file, String filename) {
        try {
            MessagePayload payload = new MessagePayload();
            payload.setMessage(message);

            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                payload.setFile(base64);
                payload.setFilename(filename);
            }

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(telegram, json);

            log.info("KafkaProducerService - sendFileToTelegram. Mensaje enviado: {}", message);
        } catch (Exception e) {
            log.error("Error serializando/enviando archivo a Telegram: {}", e.getMessage(), e);
        }
    }

}