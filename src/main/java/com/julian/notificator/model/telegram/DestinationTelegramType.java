package com.julian.notificator.model.telegram;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DestinationTelegramType {
    CHANNELS,
    BOT,
    GROUPS,
    ALL; 
    
    @JsonCreator
    public static DestinationTelegramType fromString(String value) {
        if (value == null) {
            return ALL; 
        }
        try {
            return DestinationTelegramType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Valor inválido para 'destination'. Valores válidos: CHANNELS, BOT, GROUPS, ALL"
                );
        }
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase(); 
    }
}
