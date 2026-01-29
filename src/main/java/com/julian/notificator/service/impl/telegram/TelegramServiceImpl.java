package com.julian.notificator.service.impl.telegram;

import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("telegramServiceImpl")
public class TelegramServiceImpl implements NotificationService {

    private static final String DOCUMENT = "document";
    private static final String TEXT = "text";
    private static final String CHAT_ID = "chat_id";
    private static final String PHOTO = "photo";
    private static final String CAPTION = "caption";

    @Value("${telegram.proxy-url}")
    private String telegramProxyUrl;

    @Value("${telegram.chat-id}")
    private String chatIdUser;

    @Value("${telegram.chat-id-group}")
    private String chatIdGroup;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String message) {
        sendTextToUserAndGroup(message);
    }
    
    @Override
    public void sendPinMessage(String message) {

        String chatId = chatIdGroup; 
        try {
            
            Map<String, String> body = Map.of(
                    CHAT_ID, chatId,
                    TEXT, message
            );
            var response = restTemplate.postForEntity(telegramProxyUrl + "/sendMessage", body, String.class);
            log.debug("Mensaje enviado a Telegram chat_id {}: {}", chatId, message);

            String responseBody = response.getBody();
            if (responseBody != null && responseBody.contains("message_id")) {
                int messageId = extractMessageId(responseBody);
                pinMessage(chatId, messageId);
            }
        } catch (Exception e) {
            log.error("Error enviando y anclando mensaje en Telegram chat_id {}: {}", chatId, e.getMessage());
        }
    }

    @Override
    public void sendMessageFile(MessagePayload payload) {
        String filename = payload.getFilename();

        if (payload.getFile() == null || payload.getFile().isBlank()) {
            sendTextToUserAndGroup(payload.getMessage());
            return;
        }

        byte[] fileBytes = Base64.getDecoder().decode(payload.getFile());
        boolean isImage = isImage(filename);

        sendToUserAndGroupWithFile(payload.getMessage(), fileBytes, filename, isImage);
    }


    /* ============================================================
       ENVÍO A USUARIO Y GRUPO
       ============================================================ */

    private void sendTextToUserAndGroup(String message) {
        sendText(chatIdUser, message);
        sendText(chatIdGroup, message);
    }

    private void sendToUserAndGroupWithFile(String message, byte[] bytes, String filename, boolean isImage) {
        sendToChatWithFile(chatIdUser, message, bytes, filename, isImage);
        sendToChatWithFile(chatIdGroup, message, bytes, filename, isImage);
    }

    private void sendToChatWithFile(String chatId, String message, byte[] bytes, String filename, boolean isImage) {
        try {
            if (isImage) {
                sendImage(chatId, message, bytes, filename);
            } else {
                sendDocument(chatId, message, bytes, filename);
            }
        } catch (Exception e) {
            log.error("Error enviando archivo a Telegram chat_id {}: {}", chatId, e.getMessage());
        }
    }


    /* ============================================================
       DETECCIÓN DEL TIPO DE ARCHIVO
       ============================================================ */

    private boolean isImage(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
               lower.endsWith(".png") || lower.endsWith(".gif") ||
               lower.endsWith(".bmp") || lower.endsWith(".webp");
    }


    /* ============================================================
       MÉTODOS DE ENVÍO TEXT / IMAGE / DOCUMENT
       ============================================================ */

    private void sendText(String chatId, String message) {
        try {
            Map<String, String> body = Map.of(
                    CHAT_ID, chatId,
                    TEXT, message
            );
            restTemplate.postForEntity(telegramProxyUrl + "/sendMessage", body, String.class);
            log.debug("Texto enviado a Telegram chat_id {}: {}", chatId, message);
        } catch (Exception e) {
            log.error("Error enviando texto a Telegram chat_id {}: {}", chatId, e.getMessage());
        }
    }

    private void sendImage(String chatId, String caption, byte[] imageBytes, String filename) {
        try {
            LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add(CHAT_ID, chatId);
            form.add(CAPTION, caption);
            form.add(PHOTO, new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);
            restTemplate.postForEntity(telegramProxyUrl + "/sendPhoto", entity, String.class);

            log.debug("Imagen enviada a Telegram chat_id {} con mensaje: {}", chatId, caption);
        } catch (Exception e) {
            log.error("Error enviando imagen a Telegram chat_id {}: {}", chatId, e.getMessage());
        }
    }

    private void sendDocument(String chatId, String caption, byte[] fileBytes, String filename) {
        try {
            LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add(CHAT_ID, chatId);
            form.add(CAPTION, caption);
            form.add(DOCUMENT, new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);
            restTemplate.postForEntity(telegramProxyUrl + "/sendDocument", entity, String.class);

            log.debug("Documento enviado a Telegram chat_id {} con mensaje: {}", chatId, caption);
        } catch (Exception e) {
            log.error("Error enviando documento a Telegram chat_id {}: {}", chatId, e.getMessage());
        }
    }
    
    private void pinMessage(String chatId, int messageId) {
        try {
            Map<String, Object> body = Map.of(
                    CHAT_ID, chatId,
                    "message_id", messageId,
                    "disable_notification", true
            );

            restTemplate.postForEntity(telegramProxyUrl + "/pinChatMessage", body, String.class);
            log.debug("Mensaje anclado en chat_id {} con message_id {}", chatId, messageId);
        } catch (Exception e) {
            log.error("Error anclando mensaje en Telegram chat_id {}: {}", chatId, e.getMessage());
        }
    }

    private int extractMessageId(String json) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(json);
        return node.path("result").path("message_id").asInt();
    }

    @Override
    public String getChannelName() {
        return "Telegram";
    }
}
