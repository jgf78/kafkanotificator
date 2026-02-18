package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.entity.Subscribers;

public interface SubscriberService {

    Subscribers subscribe(String name, String callbackUrl);

    List<Subscribers> getActiveSubscribers();

    void notifyAllSubscribers(String eventType, Object payload);

    void deactivateSubscriber(Long id);

}

