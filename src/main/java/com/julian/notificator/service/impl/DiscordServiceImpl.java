package com.julian.notificator.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.service.DiscordService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiscordServiceImpl implements DiscordService {

    @Value("${discord.webhook-url}")
    private String discordWebhookUrl;

    @Override
    public void sendMessageToDiscord(String message) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> payload = Map.of("content", message);
        restTemplate.postForEntity(discordWebhookUrl, payload, String.class);
        log.debug("DiscordService - sendMessageToDiscord, 📤 Mensaje enviado a Discord: {}", message);
    }
}
