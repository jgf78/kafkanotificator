package com.julian.notificator.service;

import org.springframework.web.multipart.MultipartFile;

import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.telegram.TelegramPollRequest;

public interface KafkaProducerService {
    void sendMessage(String message, DestinationType destination);

    void sendFileToTelegram(String message, MultipartFile file, String filename);

    void sendPinMessage(String pinMessage);

    void sendPoll(TelegramPollRequest request);
}
