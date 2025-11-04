package com.julian.notificator.service.impl.telegram;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramServiceImpl implements NotificationService {

    @Value("${telegram.proxy-url}")
    private String telegramProxyUrl;
    @Value("${telegram.chat-id}")
    private String telegramChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String message) {
        Map<String, String> payload = Map.of(
            "chat_id", telegramChatId,
            "text", message
        );
        restTemplate.postForEntity(telegramProxyUrl, payload, String.class);
    }


    @Override
    public String getChannelName() {
        return "Telegram";
    }
}
