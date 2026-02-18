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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.entity.Subscribers;
import com.julian.notificator.entity.WebhookDeliveryLog;
import com.julian.notificator.model.subscriber.WebhookEvent;
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

    private final SubscriberRepository repository;
    private final RestTemplate restTemplate;
    private final WebhookDeliveryLogRepository logRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Subscribers subscribe(String name, String callbackUrl) {

        log.debug("SubscriberService - subscribe");

        if (repository.existsByCallbackUrl(callbackUrl)) {
            log.error("Callback already registered");
            throw new IllegalArgumentException("Callback already registered");
        }

        Subscribers subscriber = new Subscribers();
        subscriber.setName(name);
        subscriber.setCallbackUrl(callbackUrl);
        subscriber.setApiKey(generateApiKey());
        subscriber.setActive(true);
        subscriber.setFailureCount(0);
        subscriber.setCreatedAt(LocalDateTime.now());

        return repository.save(subscriber);
    }

    @Override
    public List<Subscribers> getActiveSubscribers() {
        log.debug("SubscriberService - getActiveSubscribers");
        return repository.findByActiveTrue();
    }

    @Async
    @Transactional
    @Override
    public void notifyAllSubscribers(String eventType, Object payload) {
        log.debug("SubscriberService - notifyAllSubscribers");

        List<Subscribers> subscribers = repository.findByActiveTrue();

        for (Subscribers subscriber : subscribers) {
            sendWebhook(subscriber, eventType, payload);
        }
    }

    private void sendWebhook(Subscribers subscriber, String eventType, Object payload) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Notificator-Key", subscriber.getApiKey());

            WebhookEvent event = new WebhookEvent(eventType, payload, LocalDateTime.now());

            HttpEntity<WebhookEvent> entity = new HttpEntity<>(event, headers);

            ResponseEntity<Void> response = restTemplate.exchange(subscriber.getCallbackUrl(), HttpMethod.POST, entity,
                    Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                subscriber.setFailureCount(0);
            } else {
                handleFailure(subscriber);
            }

        } catch (Exception ex) {
            handleFailure(subscriber);
        }

        repository.save(subscriber);
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
        repository.findById(id).ifPresent(sub -> {
            sub.setActive(false);
            repository.save(sub);
        });
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void sendWebhookAsync(Subscribers subscriber, String eventType, Object payload, int attempt) {

        WebhookDeliveryLog log = new WebhookDeliveryLog();
        log.setSubscriber(subscriber);
        log.setEventType(eventType);

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            log.setPayload(jsonPayload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Notificator-Key", subscriber.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<Void> response = restTemplate.exchange(subscriber.getCallbackUrl(), HttpMethod.POST, entity,
                    Void.class);

            log.setStatusCode(response.getStatusCode().value());

            log.setErrorMessage(null);

            if (!response.getStatusCode().is2xxSuccessful()) {
                handleFailureWithRetry(subscriber, log, eventType, payload, attempt);
            }

        } catch (Exception ex) {
            log.setErrorMessage(ex.getMessage());
            handleFailureWithRetry(subscriber, log, eventType, payload, attempt);
        } finally {
            logRepository.save(log);
        }
    }

    private void handleFailureWithRetry(Subscribers subscriber, WebhookDeliveryLog log, String eventType,
            Object payload, int attempt) {

        subscriber.setFailureCount(subscriber.getFailureCount() + 1);

        if (subscriber.getFailureCount() >= MAX_FAILURES) {
            subscriber.setActive(false);
        }

        repository.save(subscriber);

        // Reintento máximo 3 veces
        if (attempt < 3 && subscriber.isActive()) {
            try {
                Thread.sleep(2000); // espera 2 segundos entre reintentos
            } catch (InterruptedException ignored) {
            }

            sendWebhookAsync(subscriber, eventType, payload, attempt + 1);
        }
    }

}
