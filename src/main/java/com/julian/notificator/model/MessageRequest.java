package com.julian.notificator.model;

import com.julian.notificator.model.telegram.DestinationTelegramType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;
    private DestinationType destination = DestinationType.ALL; 
    private DestinationTelegramType destinationTelegram = DestinationTelegramType.ALL;
}
