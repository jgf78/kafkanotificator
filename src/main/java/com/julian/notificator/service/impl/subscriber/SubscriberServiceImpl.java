package com.julian.notificator.service.impl.subscriber;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.entity.Subscribers;
import com.julian.notificator.model.subscriber.WebhookEvent;
import com.julian.notificator.repository.SubscriberRepository;
import com.julian.notificator.service.SubscriberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SubscriberServiceImpl implements SubscriberService {

    private static final int MAX_FAILURES = 5;

    private final SubscriberRepository repository;
    private final RestTemplate restTemplate;

    @Override
    public Subscribers subscribe(String name, String callbackUrl) {

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

        return repository.save(subscriber);
    }

    @Override
    public List<Subscribers> getActiveSubscribers() {
        return repository.findByActiveTrue();
    }

    @Override
    public void notifyAllSubscribers(String eventType, Object payload) {

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

            WebhookEvent event = new WebhookEvent(
                    eventType,
                    payload,
                    LocalDateTime.now()
            );

            HttpEntity<WebhookEvent> entity = new HttpEntity<>(event, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    subscriber.getCallbackUrl(),
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

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

}

