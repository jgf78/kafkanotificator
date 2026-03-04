package com.julian.notificator.service;

import com.julian.notificator.model.webhook.NewsWebhookRequest;

public interface WebhookService {

    void setData(NewsWebhookRequest request);

}
