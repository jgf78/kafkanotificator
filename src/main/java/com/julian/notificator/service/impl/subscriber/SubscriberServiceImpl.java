package com.julian.notificator.service.impl.subscriber;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.entity.Subscribers;
import com.julian.notificator.entity.WebhookDeliveryLog;
import com.julian.notificator.model.subscriber.SubscriberResponse;
import com.julian.notificator.model.subscriber.WebhookEventType;
import com.julian.notificator.repository.SubscriberRepository;
import com.julian.notificator.repository.WebhookDeliveryLogRepository;
import com.julian.notificator.service.SubscriberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriberServiceImpl implements SubscriberService {

    private static final int MAX_FAILURES = 5;
    private static final int MAX_RETRIES = 3;

    private final SubscriberRepository repository;
    private final WebhookDeliveryLogRepository logRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Subscribers subscribe(String name, String callbackUrl, List<WebhookEventType> events) {
        
        log.debug("SubscriberService - subscribe");

        if (repository.existsByCallbackUrl(callbackUrl)) {
            throw new IllegalArgumentException("Callback already registered");
        }

        Subscribers subscriber = new Subscribers();
        subscriber.setName(name);
        subscriber.setCallbackUrl(callbackUrl);
        subscriber.setApiKey(generateApiKey());
        subscriber.setActive(true);
        subscriber.setFailureCount(0);
        subscriber.setCreatedAt(LocalDateTime.now());
        subscriber.setEvents(events);

        return repository.save(subscriber);
    }
    
    @Transactional
    @Override
    public Subscribers updateEvents(Long subscriberId, List<WebhookEventType> events) {
        
        log.debug("SubscriberService - updateEvents");

        Subscribers subscriber = repository.findById(subscriberId)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        subscriber.setEvents(events);

        return repository.save(subscriber);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SubscriberResponse> getActiveSubscribers() {

        log.debug("SubscriberService - getActiveSubscribers");
        
        return repository.findActiveSubscribersWithEvents()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SubscriberResponse mapToResponse(Subscribers sub) {

        SubscriberResponse response = new SubscriberResponse();
        response.setId(sub.getId());
        response.setName(sub.getName());
        response.setCallbackUrl(sub.getCallbackUrl());
        response.setActive(sub.isActive());
        response.setEvents(sub.getEvents());

        return response;
    }

    @Async
    @Override
    public void notifyAllSubscribers(String eventType, Object payload) {
        log.debug("SubscriberService - notifyAllSubscribers");

        List<Subscribers> subscribers = repository.findByActiveTrue()
                .stream()
                .filter(sub -> sub.getEvents().contains(WebhookEventType.valueOf(eventType)))
                .toList();

        for (Subscribers subscriber : subscribers) {
            sendWebhook(subscriber, eventType, payload, 1);
        }
    }

    private void sendWebhook(Subscribers subscriber, String eventType, Object payload, int attempt) {

        WebhookDeliveryLog deliveryLog = new WebhookDeliveryLog();
        deliveryLog.setSubscriber(subscriber);
        deliveryLog.setEventType(eventType);
        deliveryLog.setAttemptedAt(LocalDateTime.now());

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            deliveryLog.setPayload(jsonPayload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Notificator-Key", subscriber.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<Void> response = restTemplate.exchange(subscriber.getCallbackUrl(), HttpMethod.POST, entity,
                    Void.class);

            int statusCode = response.getStatusCode().value();
            deliveryLog.setStatusCode(statusCode);

            if (response.getStatusCode().is2xxSuccessful()) {
                // Reset failures
                subscriber.setFailureCount(0);
                deliveryLog.setErrorMessage(null);

            } else {
                handleFailure(subscriber);
                retryIfPossible(subscriber, eventType, payload, attempt);
            }

        } catch (Exception ex) {

            deliveryLog.setStatusCode(null);
            deliveryLog.setErrorMessage(ex.getMessage());

            handleFailure(subscriber);
            retryIfPossible(subscriber, eventType, payload, attempt);

        } finally {
            repository.save(subscriber);
            persistLog(deliveryLog);
        }
    }

    private void retryIfPossible(Subscribers subscriber, String eventType, Object payload, int attempt) {

        if (attempt < MAX_RETRIES && subscriber.isActive()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }

            sendWebhook(subscriber, eventType, payload, attempt + 1);
        }
    }

    private void handleFailure(Subscribers subscriber) {

        int failures = subscriber.getFailureCount() + 1;
        subscriber.setFailureCount(failures);

        if (failures >= MAX_FAILURES) {
            subscriber.setActive(false);
        }
    }

    @Override
    public void deactivateSubscriber(Long id) {

        log.debug("SubscriberService - deactivateSubscriber");

        repository.findById(id).ifPresent(sub -> {
            sub.setActive(false);
            repository.save(sub);
        });
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void persistLog(WebhookDeliveryLog log) {
        logRepository.save(log);
    }

    @Override
    public  WebhookEventType[] getEvents() {
        
        log.debug("SubscriberService - getEvents");
        return WebhookEventType.values();
    }

}
