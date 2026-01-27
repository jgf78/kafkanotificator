package com.julian.notificator.service.impl.telegram;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.julian.notificator.model.cinema.TmdbMovie;
import com.julian.notificator.service.CinemaDataService;
import com.julian.notificator.service.FootballDataService;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.bot-username}")
    private String botUsername;

    @Value("${telegram.backend-url}")
    private String backendUrl;

    private final RestTemplate restTemplate;
    private final FootballDataService footballDataService;
    private final CinemaDataService cinemaDataService;

    private Map<String, Consumer<Long>> commandHandlers;

    public TelegramBot(RestTemplate restTemplate,
                       FootballDataService footballDataService,
                       CinemaDataService cinemaDataService) {

        this.restTemplate = restTemplate;
        this.footballDataService = footballDataService;
        this.cinemaDataService = cinemaDataService;
    }

    // =======================
    // INIT COMMANDS
    // =======================

    @PostConstruct
    private void initCommands() {

        commandHandlers = Map.of(
            "/titulares", this::handleTitulares,
            "/realmadrid", this::handleRealMadrid,
            "/cartelera", this::handleCartelera
        );
    }

    // =======================
    // UPDATE RECEIVED
    // =======================

    @Override
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String texto = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // Elimina @BotName si viene
        String comando = texto.split("@")[0].toLowerCase();

        Consumer<Long> handler = commandHandlers.get(comando);

        if (handler != null) {
            handler.accept(chatId);
        } else {
            sendText(chatId, "🤖 Comando no reconocido.\nPrueba con /cartelera, /titulares o /realmadrid");
        }
    }

    // =======================
    // COMMAND HANDLERS
    // =======================

    private void handleTitulares(Long chatId) {
        try {
            String headlines = restTemplate.getForObject(backendUrl, String.class);
            sendText(chatId,
                    headlines != null
                            ? headlines
                            : "📰 No se han podido obtener los titulares.");
        } catch (Exception e) {
            logger.error("Error en comando /titulares", e);
            sendText(chatId, "❌ Error al obtener los titulares.");
        }
    }

    private void handleRealMadrid(Long chatId) {
        try {
            sendText(chatId, footballDataService.formatLiveMatchMessage());
        } catch (Exception e) {
            logger.error("Error en comando /realmadrid", e);
            sendText(chatId, "❌ Error al obtener el resultado de fútbol.");
        }
    }

    private void handleCartelera(Long chatId) {

        sendText(chatId, "🍿 Consultando la cartelera actual...");

        try {
            List<TmdbMovie> movies = cinemaDataService.getTop10NowPlaying();

            if (movies.isEmpty()) {
                sendText(chatId, "🎬 No se ha podido obtener la cartelera en este momento.");
                return;
            }

            sendText(chatId, cinemaDataService.buildCarteleraMessage(movies));

        } catch (Exception e) {
            logger.error("Error en comando /cartelera", e);
            sendText(chatId, "❌ Error al obtener la cartelera de cine.");
        }
    }

    // =======================
    // SEND MESSAGE
    // =======================

    private void sendText(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.enableMarkdown(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error enviando mensaje a Telegram", e);
        }
    }

    // =======================
    // BOT CONFIG
    // =======================

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
