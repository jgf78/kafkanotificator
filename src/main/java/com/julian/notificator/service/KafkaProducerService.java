package com.julian.notificator.service;

import org.springframework.web.multipart.MultipartFile;

import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.TelegramPollRequest;

public interface KafkaProducerService {
    
    void sendPinMessage(String pinMessage);

    void sendPoll(TelegramPollRequest request);

    void sendFile(String message, MultipartFile file, String filename, DestinationType destination);

    void sendMessage(MessageRequest request);
}
