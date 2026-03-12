package com.julian.notificator.model.transport;

public record TelegramStop(
        String displayName,
        String type,
        double lat,
        double lon,
        String accessibility,
        String ref,
        String website
    ) {}
