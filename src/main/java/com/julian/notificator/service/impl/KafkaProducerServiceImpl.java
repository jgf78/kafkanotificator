package com.julian.notificator.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.TelegramPollRequest;
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

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaProducerServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(MessageRequest request) {
        
        switch (request.getDestination()) {
        case DISCORD -> kafkaTemplate.send(discord, request);
        case TELEGRAM -> kafkaTemplate.send(telegram, request);
        case MAIL -> kafkaTemplate.send(mail, request);
        case ALEXA -> kafkaTemplate.send(alexa, request);
        case WHATSAPP -> kafkaTemplate.send(whatsapp, request);
        case MQTT -> kafkaTemplate.send(mqtt, request);
        case ALL -> {
            kafkaTemplate.send(discord, request);
            kafkaTemplate.send(telegram, request);
            kafkaTemplate.send(mail, request);
            kafkaTemplate.send(alexa, request);
            kafkaTemplate.send(whatsapp, request);
            kafkaTemplate.send(mqtt, request);
            }
        }
        log.info("KafkaProducerService - sendMessage. Mensaje enviado a {}: {}", request.getDestination(), request.getMessage());

    }

    @Override
    public void sendFile(String message, MultipartFile file, String filename, DestinationType destination) {
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
            switch (destination) {
                case DISCORD -> kafkaTemplate.send(discord, json);
                case TELEGRAM -> kafkaTemplate.send(telegram, json);
                case MAIL -> kafkaTemplate.send(mail, json);
                default -> log.warn("Destino {} no implementado, mensaje ignorado", destination);
            }

            log.info("KafkaProducerService - sendFile. Mensaje enviado: {}", message);
        } catch (Exception e) {
            log.error("Error serializando/enviando archivo: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendPinMessage(String pinMessage) {
        try {
            MessagePayload payload = new MessagePayload();
            payload.setMessage(pinMessage);
            payload.setPin(true); 

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(telegram, json);

            log.info("KafkaProducerService - sendPinMessage. Mensaje anclado enviado {}", pinMessage);
        } catch (Exception e) {
            log.error("Error enviando mensaje anclado a Telegram: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendPoll(TelegramPollRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(telegram, json);

            log.info("KafkaProducerService - sendPoll. Encuesta enviada con la pregunta {}", request.getQuestion());
        } catch (Exception e) {
            log.error("Error enviando encuesta a Telegram: {}", e.getMessage(), e);
        }
        
    }


}