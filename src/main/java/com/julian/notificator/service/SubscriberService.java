package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.entity.Subscribers;
import com.julian.notificator.model.subscriber.WebhookEventType;

public interface SubscriberService {

    List<Subscribers> getActiveSubscribers();

    void notifyAllSubscribers(String eventType, Object payload);

    void deactivateSubscriber(Long id);

    Subscribers subscribe(String name, String callbackUrl, List<WebhookEventType> events);

    Subscribers updateEvents(Long subscriberId, List<WebhookEventType> events);

}

