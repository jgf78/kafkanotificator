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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("telegramServiceImpl")
public class TelegramServiceImpl implements NotificationService {

    private static final String CHAT_ID = "chat_id";
    private static final String TEXT = "text";
    private static final String CAPTION = "caption";
    private static final String PHOTO = "photo";
    private static final String VIDEO = "video";
    private static final String DOCUMENT = "document";

    @Value("${telegram.proxy-url}")
    private String telegramProxyUrl;

    @Value("${telegram.chat-id}")
    private String chatIdUser;

    @Value("${telegram.chat-id-group}")
    private String chatIdGroup;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /* ============================================================
       API PÚBLICA
       ============================================================ */

    @Override
    public void sendMessage(String message) {
        sendTextToUserAndGroup(message);
    }

    @Override
    public void sendPinMessage(String message) {
        try {
            String response = sendText(chatIdGroup, message);
            int messageId = extractMessageId(response);
            pinMessage(chatIdGroup, messageId);
        } catch (Exception e) {
            log.error("Error enviando y anclando mensaje Telegram: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMessageFile(MessagePayload payload) {

        if (payload.getFile() == null || payload.getFile().isBlank()) {
            sendTextToUserAndGroup(payload.getMessage());
            return;
        }

        byte[] bytes = Base64.getDecoder().decode(payload.getFile());
        String filename = payload.getFilename().toLowerCase();

        FileType type = detectFileType(filename);

        sendFileToUserAndGroup(
                payload.getMessage(),
                bytes,
                payload.getFilename(),
                type
        );
    }

    @Override
    public String getChannelName() {
        return "Telegram";
    }

    /* ============================================================
       ENVÍO A USER / GROUP
       ============================================================ */

    private void sendTextToUserAndGroup(String message) {
        sendText(chatIdUser, message);
        sendText(chatIdGroup, message);
    }

    private void sendFileToUserAndGroup(String caption, byte[] bytes, String filename, FileType type) {
        sendFile(chatIdUser, caption, bytes, filename, type);
        sendFile(chatIdGroup, caption, bytes, filename, type);
    }

    /* ============================================================
       ENVÍO BASE
       ============================================================ */

    private String sendText(String chatId, String message) {
        Map<String, String> body = Map.of(
                CHAT_ID, chatId,
                TEXT, message
        );

        var response = restTemplate.postForEntity(
                telegramProxyUrl + "/sendMessage",
                body,
                String.class
        );

        log.debug("Texto enviado a Telegram chat_id {}: {}", chatId, message);
        return response.getBody();
    }

    private void sendFile(String chatId, String caption, byte[] bytes, String filename, FileType type) {

        String endpoint;
        String field;

        switch (type) {
            case IMAGE -> {
                endpoint = "/sendPhoto";
                field = PHOTO;
            }
            case VIDEO -> {
                endpoint = "/sendVideo";
                field = VIDEO;
            }
            default -> {
                endpoint = "/sendDocument";
                field = DOCUMENT;
            }
        }

        try {
            LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add(CHAT_ID, chatId);
            form.add(CAPTION, caption);
            form.add(field, new TelegramFileResource(bytes, filename));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> entity =
                    new HttpEntity<>(form, headers);

            restTemplate.postForEntity(
                    telegramProxyUrl + endpoint,
                    entity,
                    String.class
            );

            log.debug("{} enviado a Telegram chat_id {} con caption: {}", type, chatId, caption);

        } catch (Exception e) {
            log.error("Error enviando {} a Telegram chat_id {}: {}", type, chatId, e.getMessage(), e);
        }
    }

    /* ============================================================
       PIN MESSAGE
       ============================================================ */

    private void pinMessage(String chatId, int messageId) {
        Map<String, Object> body = Map.of(
                CHAT_ID, chatId,
                "message_id", messageId,
                "disable_notification", true
        );

        restTemplate.postForEntity(
                telegramProxyUrl + "/pinChatMessage",
                body,
                String.class
        );

        log.debug("Mensaje anclado en chat_id {} con message_id {}", chatId, messageId);
    }

    /* ============================================================
       UTILIDADES
       ============================================================ */

    private int extractMessageId(String json) throws Exception {
        return objectMapper.readTree(json)
                .path("result")
                .path("message_id")
                .asInt();
    }

    private FileType detectFileType(String filename) {

        if (filename.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            return FileType.IMAGE;
        }

        if (filename.matches(".*\\.(mp4|mov|m4v|webm|mkv|avi)$")) {
            return FileType.VIDEO;
        }

        return FileType.DOCUMENT;
    }

    /* ============================================================
       TIPOS Y CLASES AUXILIARES
       ============================================================ */

    private enum FileType {
        IMAGE, VIDEO, DOCUMENT
    }

    private static class TelegramFileResource extends ByteArrayResource {

        private final String filename;

        TelegramFileResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
