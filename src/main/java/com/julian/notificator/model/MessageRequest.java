package com.julian.notificator.model;

import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.model.telegram.TelegramPollRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;
    private DestinationType destination = DestinationType.ALL; 
    
    //Only for Telegram
    private DestinationTelegramType destinationTelegram = DestinationTelegramType.ALL;
    private TelegramPollRequest telegramPollRequest;
    private MessagePayload messagePayload;
}
