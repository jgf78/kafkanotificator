package com.julian.notificator.service.impl.discord;

import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.AbstractNotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiscordServiceImpl extends AbstractNotificationService {

    private static final String DISCORD = "Discord";
    
    @Value("${discord.webhook-url-news}")
    private String discordWebhookUrl;

    @Override
    public void sendMessage(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> payload = Map.of("content", message);
            restTemplate.postForEntity(discordWebhookUrl, payload, String.class);
            log.info("DiscordService - sendMessage, 📤 Mensaje enviado a Discord: {}", message);
        } catch (Exception e) {
            log.error("❌ Error enviando mensaje a Discord: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendMessageFile(MessagePayload payload) {
        try {
            byte[] fileBytes = Base64.getDecoder().decode(payload.getFile());
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return payload.getFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("content", payload.getMessage());
            body.add("file", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(discordWebhookUrl, request, String.class);
            log.info("DiscordService - sendMessageFile, ✅ Mensaje + archivo enviado a Discord: {}", payload.getFilename());

        } catch (Exception e) {
            log.error("❌ Error enviando archivo a Discord: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getChannelName() {
        return DISCORD;
    }

}
