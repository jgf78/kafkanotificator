package com.julian.notificator.service.impl.telegram;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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
import com.julian.notificator.model.weather.WeeklyWeather;
import com.julian.notificator.service.CinemaDataService;
import com.julian.notificator.service.FootballDataService;
import com.julian.notificator.service.WeatherService;

import jakarta.annotation.PostConstruct;

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
    private final WeatherService weatherService;

    private Map<String, BiConsumer<Long, String>> commandHandlers;

    public TelegramBot(RestTemplate restTemplate,
                       FootballDataService footballDataService,
                       CinemaDataService cinemaDataService,
                       WeatherService weatherService) {

        this.restTemplate = restTemplate;
        this.footballDataService = footballDataService;
        this.cinemaDataService = cinemaDataService;
        this.weatherService = weatherService;
    }

    // =======================
    // INIT COMMANDS
    // =======================

    @PostConstruct
    private void initCommands() {

        commandHandlers = Map.of(
            "/titulares", (chatId, text) -> handleTitulares(chatId),
            "/realmadrid", (chatId, text) -> handleRealMadrid(chatId),
            "/cartelera", (chatId, text) -> handleCartelera(chatId),
            "/tiempo", this::handleTiempo
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

        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();

        String command = text.split(" ")[0].split("@")[0].toLowerCase();

        BiConsumer<Long, String> handler = commandHandlers.get(command);

        if (handler != null) {
            handler.accept(chatId, text);
        }
    }


    // =======================
    // COMMAND HANDLERS
    // =======================

    private void handleTitulares(Long chatId) {
        
        sendText(chatId, "📰 Consultando titulares de prensa...");
        
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
        
        sendText(chatId, "⚽ Consultando información del Real Madrid...");
        
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
    
    private void handleTiempo(Long chatId, String text) {

        sendText(chatId, "🌤️ Consultando el tiempo de tu ciudad...");

        try {
            String city = text.replaceFirst("/tiempo(@\\w+)?", "").trim();

            if (city.isEmpty()) {
                sendText(chatId, "❌ Uso correcto: /tiempo Madrid");
                return;
            }

            WeeklyWeather weather = weatherService.getWeeklyForecast(city);

            if (weather == null || weather.getDays() == null || weather.getDays().isEmpty()) {
                sendText(chatId, "❌ No se ha podido obtener la previsión para *" + city + "*");
                return;
            }

            String msg = weatherService.formatWeeklyWeather(weather);
            sendText(chatId, msg);

        } catch (IllegalArgumentException e) {
            sendText(chatId, "❌ No he encontrado la ciudad indicada 😕");
        } catch (Exception e) {
            logger.error("Error en comando /tiempo", e);
            sendText(chatId, "❌ Error al obtener el tiempo. Inténtalo más tarde.");
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