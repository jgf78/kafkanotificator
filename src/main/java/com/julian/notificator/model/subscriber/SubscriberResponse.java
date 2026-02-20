package com.julian.notificator.model.subscriber;

import java.util.List;

import lombok.Data;

@Data
public class SubscriberResponse {

    private Long id;
    private String name;
    private String callbackUrl;
    private boolean active;
    private List<WebhookEventType> events;
}