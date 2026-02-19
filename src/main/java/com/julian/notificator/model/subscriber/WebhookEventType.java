package com.julian.notificator.model.subscriber;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available webhook event types")
public enum WebhookEventType {

    @Schema(description = "Triggered when a Telegram text is received")
    TELEGRAM_TEXT_EVENT,

    @Schema(description = "Triggered when a live match event occurs")
    LIVE_MATCH_EVENT,

    @Schema(description = "Triggered when a user registers")
    USER_REGISTERED_EVENT
}

