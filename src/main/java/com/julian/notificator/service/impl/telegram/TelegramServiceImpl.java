package com.julian.notificator.service.impl.telegram;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.config.properties.TelegramProperties;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.telegram.TelegramPollRequest;
import com.julian.notificator.service.NotificationService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("telegramServiceImpl")
public class TelegramServiceImpl implements NotificationService {

    private static final String QUIZ = "quiz";
    private static final String ALLOWS_MULTIPLE_ANSWERS = "allows_multiple_answers";
    private static final String TYPE = "type";
    private static final String IS_ANONYMOUS = "is_anonymous";
    private static final String OPTIONS = "options";
    private static final String QUESTION = "question";
    private static final String CHAT_ID = "chat_id";
    private static final String TEXT = "text";
    private static final String CAPTION = "caption";
    private static final String PHOTO = "photo";
    private static final String VIDEO = "video";
    private static final String DOCUMENT = "document";

    private final TelegramProperties telegramProperties;
    
    public TelegramServiceImpl(TelegramProperties telegramProperties) {
        this.telegramProperties = telegramProperties;
    }
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void init() {
        log.info("Grupos Telegram cargados: {}", telegramProperties.getChatIdsGroups());
    }


    /* ============================================================
       API PÚBLICA
       ============================================================ */

    @Override
    public void sendMessage(String message) {
        sendTextToUserAndGroups(message);
    }

    @Override
    public void sendPinMessage(String message) {
        for (String chatId : telegramProperties.getChatIdsGroups()) {
            try {
                String response = sendText(chatId, message);
                int messageId = extractMessageId(response);
                pinMessage(chatId, messageId);
            } catch (Exception e) {
                log.error("Error enviando/anclando mensaje en chat {}: {}", chatId, e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendPoll(TelegramPollRequest poll) {
        for (String chatId : telegramProperties.getChatIdsGroups()) {
            try {
                Map<String, Object> body = new HashMap<>();

                body.put(CHAT_ID, chatId);
                body.put(QUESTION, poll.getQuestion());
                body.put(OPTIONS, poll.getOptions());
                body.put(IS_ANONYMOUS, poll.isAnonymous());
                body.put(ALLOWS_MULTIPLE_ANSWERS, poll.isMultipleAnswers());
                body.put(TYPE, poll.getType());

                if (QUIZ.equalsIgnoreCase(poll.getType())) {
                    body.put("correct_option_id", poll.getCorrectOptionId());
                }

                ResponseEntity<String> response = restTemplate.postForEntity(
                        telegramProperties.getProxyUrl() + "/sendPoll",
                        body,
                        String.class
                );

                log.debug("Encuesta enviada a Telegram chat_id {}: {}", chatId, poll.getQuestion());
                log.debug("Respuesta Telegram: {}", response.getBody());

            } catch (Exception e) {
                log.error("Error enviando encuesta a Telegram chat_id {}", chatId, e);
            }
        }
    }

    @Override
    public void sendMessageFile(MessagePayload payload) {

        if (payload.getFile() == null || payload.getFile().isBlank()) {
            sendTextToUserAndGroups(payload.getMessage());
            return;
        }

        byte[] bytes = Base64.getDecoder().decode(payload.getFile());
        String filename = payload.getFilename().toLowerCase();

        FileType type = detectFileType(filename);

        sendFileToUserAndGroups(
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
       ENVÍO A USER / GROUPS
       ============================================================ */

    public void sendToAllGroups(String text) {
        for (String chatId : telegramProperties.getChatIdsGroups()) {
            sendText(chatId, text);
        }
    }

    private void sendTextToUserAndGroups(String message) {
        sendText(telegramProperties.getChatId(), message);
        sendToAllGroups(message);
    }

    private void sendFileToUserAndGroups(String caption, byte[] bytes, String filename, FileType type) {
        sendFile(telegramProperties.getChatId(), caption, bytes, filename, type);
        for (String chatId : telegramProperties.getChatIdsGroups()) {
            sendFile(chatId, caption, bytes, filename, type);
        }
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
                telegramProperties.getProxyUrl() + "/sendMessage",
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
                    telegramProperties.getProxyUrl() + endpoint,
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
                telegramProperties.getProxyUrl() + "/pinChatMessage",
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
