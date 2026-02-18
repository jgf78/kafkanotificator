package com.julian.notificator.model.subscriber;

import lombok.Data;

@Data
public class SubscribeRequest {

    private String name;
    private String callbackUrl;

}

