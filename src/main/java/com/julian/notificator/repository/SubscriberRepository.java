package com.julian.notificator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.julian.notificator.entity.Subscribers;
import com.julian.notificator.model.subscriber.WebhookEventType;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscribers, Long> {

    @Query("""
            SELECT DISTINCT s
            FROM Subscribers s
            LEFT JOIN FETCH s.events
            WHERE s.active = true
            """)
    List<Subscribers> findActiveSubscribersWithEvents();

    Optional<Subscribers> findByApiKey(String apiKey);

    Optional<Subscribers> findByCallbackUrl(String callbackUrl);

    Optional<Subscribers> findByName(String name);

    List<Subscribers> findByFailureCountGreaterThanEqual(int maxFailures);

    boolean existsByCallbackUrl(String callbackUrl);

    boolean existsByApiKey(String apiKey);

    @Query("""
            SELECT DISTINCT s
            FROM Subscribers s
            JOIN s.events e
            WHERE s.active = true
            AND e = :event
            """)
     List<Subscribers> findActiveSubscribersByEvent(WebhookEventType event);

}
