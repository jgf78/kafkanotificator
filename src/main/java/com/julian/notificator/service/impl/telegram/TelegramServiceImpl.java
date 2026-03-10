package com.julian.notificator.service.impl.telegram;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.config.properties.TelegramProperties;
import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.model.telegram.TelegramPollRequest;
import com.julian.notificator.service.NotificationService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("telegramServiceImpl")
public class TelegramServiceImpl implements NotificationService {

    private static final String MESSAGE_ID = "message_id";
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
    
    private final Map<String, Map<String, Integer>> matchMessageMap = new ConcurrentHashMap<>();

    private static final Pattern MATCH_PATTERN =
            Pattern.compile("(?i).+\\s(vs\\.?|v|-)+\\s.+");

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

    public void sendOrUpdateMatchMessage(String matchKey, String fullMessage) {

        log.info("⚽ Procesando partido: {}", matchKey);

        for (String chatId : telegramProperties.getChatIdsGroups()) {

            try {

                Map<String, Integer> chatMessages =
                        matchMessageMap.computeIfAbsent(matchKey, k -> new ConcurrentHashMap<>());

                if (chatMessages.containsKey(chatId)) {

                    int messageId = chatMessages.get(chatId);

                    log.info("✏️ Editando mensaje existente chat {} messageId {}", chatId, messageId);

                    editMessage(chatId, messageId, fullMessage);

                } else {

                    log.info("📤 Enviando nuevo mensaje partido a chat {}", chatId);

                    String response = sendText(chatId, fullMessage);
                    int messageId = extractMessageId(response);

                    chatMessages.put(chatId, messageId);

                    log.info("✅ Guardado messageId {} para partido {}", messageId, matchKey);
                }

            } catch (Exception e) {
                log.error("❌ Error gestionando partido {} en chat {}",
                        matchKey, chatId, e);
            }
        }
    }
    
    private void editMessage(String chatId, int messageId, String newText) {

        Map<String, Object> body = Map.of(
                CHAT_ID, chatId,
                MESSAGE_ID, messageId,
                TEXT, newText
        );

        try {

            restTemplate.postForEntity(
                    telegramProperties.getProxyUrl() + "/editMessageText",
                    body,
                    String.class
            );

            log.debug("✏️ Mensaje editado chat {} messageId {}", chatId, messageId);

        } catch (HttpClientErrorException e) {

            String response = e.getResponseBodyAsString();

            if (response != null && response.contains("message is not modified")) {
                log.debug("⏭ Telegram indica que el mensaje no cambió, se ignora");
                return;
            }

            log.error("❌ Error editando mensaje {} en chat {}", messageId, chatId);
            throw e;
        }
    }
    
    @Override
    public void sendMessage(String message, DestinationTelegramType destination) {
        sendTextToUserAndGroups(message, destination);
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
    public void sendMessageFile(MessageRequest messageRequest, DestinationTelegramType destination) {

        MessagePayload payload = messageRequest.getMessagePayload();
        
        if (payload.getFile() == null || payload.getFile().isBlank()) {
            sendTextToUserAndGroups(messageRequest.getMessage(), destination);
            return;
        }

        byte[] bytes = Base64.getDecoder().decode(payload.getFile());
        String filename = payload.getFilename().toLowerCase();

        FileType type = detectFileType(filename);

        sendFileToUserAndGroups(
                messageRequest.getMessage(),
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
    
    public void sendToAllChannels(String text) {
        for (String chatId : telegramProperties.getChatIdsChannels()) {
            sendText(chatId, text);
        }
    }

    private void sendTextToUserAndGroups(String message, DestinationTelegramType destination) {
        
        switch (destination) {
            case BOT -> sendText(telegramProperties.getChatId(), message);
            case GROUPS -> sendToAllGroups(message);
            case CHANNELS -> sendToAllChannels(message);
            case ALL -> {
                sendText(telegramProperties.getChatId(), message);
                sendToAllGroups(message);
                sendToAllChannels(message);
                }
            }
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
                MESSAGE_ID, messageId,
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
                .path(MESSAGE_ID)
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
    
    public String extractMatchKey(String message) {

        if (message == null || message.isBlank()) return null;

        String firstLine = message.split("\\R")[0].trim();

        if (MATCH_PATTERN.matcher(firstLine).matches()) {
            return normalizeMatchKey(firstLine);
        }

        return null;
    }

    private String normalizeMatchKey(String key) {
        return key.toLowerCase()
                .replace(".", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(message, DestinationTelegramType.ALL);
        
    }
}
