package com.julian.notificator.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.julian.notificator.model.subscriber.WebhookEventType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscribers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "failure_count", nullable = false)
    private int failureCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ElementCollection(targetClass = WebhookEventType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "subscriber_events", joinColumns = @JoinColumn(name = "subscriber_id"))
    @Column(name = "event_type")
    private List<WebhookEventType> events;

}


