package com.julian.notificator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DestinationType {
    DISCORD,
    TELEGRAM,
    MAIL,
    ALEXA,
    WHATSAPP,
    ALL; 
    
    @JsonCreator
    public static DestinationType fromString(String value) {
        if (value == null) {
            return ALL; 
        }
        try {
            return DestinationType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Valor inválido para 'destination'. Valores válidos: DISCORD, TELEGRAM, ALEXA, WHATSAPP, MAIL, ALL"
                );
        }
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase(); 
    }
}
