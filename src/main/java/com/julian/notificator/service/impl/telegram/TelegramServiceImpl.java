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
    private String chatIdUser;
    
    @Value("${telegram.chat-id-group}")
    private String chatIdGroup;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String message) {
        sendToChat(chatIdUser, message);
        sendToChat(chatIdGroup, message);
    }

    private void sendToChat(String chatId, String message) {
        try {
            Map<String, String> payload = Map.of(
                "chat_id", chatId,
                "text", message
            );
            restTemplate.postForEntity(telegramProxyUrl, payload, String.class);
            log.debug("Mensaje enviado a Telegram chat_id {}: {}", chatId, message);
        } catch (Exception e) {
            log.error("Error enviando mensaje a Telegram chat_id {}: {}", chatId, e.getMessage());
        }
    }


    @Override
    public String getChannelName() {
        return "Telegram";
    }
}
