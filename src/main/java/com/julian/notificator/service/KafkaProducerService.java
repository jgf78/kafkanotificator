package com.julian.notificator.service;

import org.springframework.web.multipart.MultipartFile;

import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.DestinationTelegramType;

public interface KafkaProducerService {
    
    void sendMessage(MessageRequest request);

    void sendPoll(MessageRequest messageRequest);

    void sendPinMessage(MessageRequest messageRequest);

    void sendFile(String message, MultipartFile file, String filename, DestinationType destination,
            DestinationTelegramType destinationTelegram);
}
