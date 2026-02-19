package com.julian.notificator.model.subscriber;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SubscribeRequest {

    private String name;
    private String callbackUrl;
    
    @Schema(
            description = "List of event types the subscriber wants to receive",
            implementation = WebhookEventType.class
        )
    private List<WebhookEventType> events;

}

