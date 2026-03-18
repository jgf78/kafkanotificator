package com.julian.notificator.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.MessageRequest;
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
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setMessage(message);
            messageRequest.setDestination(destination);
            MessagePayload messagePayload = new MessagePayload();
            messageRequest.setMessagePayload(messagePayload);
            
            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                messagePayload.setFile(base64);
                messagePayload.setFilename(filename);
            }

            switch (destination) {
                case DISCORD -> kafkaTemplate.send(discord, messageRequest);
                case TELEGRAM -> kafkaTemplate.send(telegram, messageRequest);
                case MAIL -> kafkaTemplate.send(mail, messageRequest);
                default -> log.warn("Destino {} no implementado, mensaje ignorado", destination);
            }

            log.info("KafkaProducerService - sendFile. Mensaje enviado: {}", message);
        } catch (Exception e) {
            log.error("Error serializando/enviando archivo: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendPinMessage(MessageRequest messageRequest) {
        try {
            MessagePayload payload = new MessagePayload();
            payload.setPin(true); 
            messageRequest.setMessagePayload(payload);

            kafkaTemplate.send(telegram, messageRequest);

            log.info("KafkaProducerService - sendPinMessage. Mensaje anclado enviado {}", messageRequest.getMessage());
        } catch (Exception e) {
            log.error("Error enviando mensaje anclado a Telegram: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendPoll(MessageRequest messageRequest) {
        messageRequest.setDestination(DestinationType.TELEGRAM);
        try {
            kafkaTemplate.send(telegram, messageRequest);

            log.info("KafkaProducerService - sendPoll. Encuesta enviada con la pregunta {}", messageRequest.getTelegramPollRequest().getQuestion());
        } catch (Exception e) {
            log.error("Error enviando encuesta a Telegram: {}", e.getMessage(), e);
        }
        
    }


}