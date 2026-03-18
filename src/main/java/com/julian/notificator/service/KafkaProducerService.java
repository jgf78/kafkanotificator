package com.julian.notificator.service;

import org.springframework.web.multipart.MultipartFile;

import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.MessageRequest;

public interface KafkaProducerService {
    
    void sendFile(String message, MultipartFile file, String filename, DestinationType destination);

    void sendMessage(MessageRequest request);

    void sendPoll(MessageRequest messageRequest);

    void sendPinMessage(MessageRequest messageRequest);
}
