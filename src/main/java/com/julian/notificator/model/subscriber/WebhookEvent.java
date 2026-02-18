package com.julian.notificator.model.subscriber;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookEvent {

    private String eventType;
    private Object data;
    private LocalDateTime timestamp;
}

