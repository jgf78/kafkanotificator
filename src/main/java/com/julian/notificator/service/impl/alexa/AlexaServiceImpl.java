package com.julian.notificator.service.impl.alexa;

import org.springframework.stereotype.Service;

import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("alexaServiceImpl")
public class AlexaServiceImpl implements NotificationService {

    private String lastMessage = "No hay mensajes nuevos";

    @Override
    public void sendMessage(String message) {
        log.debug("AlexaService - mensaje recibido: {}", message);
        this.lastMessage = message;
    }

    @Override
    public String getChannelName() {
        return "Alexa";
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
