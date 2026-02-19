package com.julian.notificator.model.subscriber;

import java.util.List;

import lombok.Data;

@Data
public class SubscribeRequest {

    private String name;
    private String callbackUrl;
    private List<WebhookEventType> events;

}

