package com.julian.notificator.model.subscriber;

import lombok.Data;

@Data
public class TestNotificationRequest {

    private String eventType;
    private Object data;

}
