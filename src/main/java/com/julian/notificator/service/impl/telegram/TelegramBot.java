package com.julian.notificator.service.impl.telegram;

import java.util.List;

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

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.bot-username}")
    private String botUsername;

    @Value("${telegram.backend-url}")  
    private String backendUrl;
    
    @Value("${telegram.backend-url2}")  
    private String backendUrlFootball;

    private final RestTemplate restTemplate;
    private final FootballDataService footballDataService;
    private final CinemaDataService cinemaDataService;

    public TelegramBot(RestTemplate restTemplate, FootballDataService footballDataService, CinemaDataService cinemaDataService) {
        this.restTemplate = restTemplate;
        this.footballDataService = footballDataService;
        this.cinemaDataService = cinemaDataService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String texto = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            
            String comando = texto.split("@")[0];
            
            if ("/titulares".equalsIgnoreCase(comando)) {
                try {
                    String headlines = restTemplate.getForObject(backendUrl, String.class);
                    sendText(chatId, headlines != null ? headlines : "No se han podido obtener los titulares.");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendText(chatId, "Error al obtener los titulares.");
                }
            }else if ("/realmadrid".equalsIgnoreCase(comando)) {
                try {
                    String resultLiveFormatted = footballDataService.formatLiveMatchMessage();
                    sendText(chatId, resultLiveFormatted);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendText(chatId, "Error al obtener el resultado de fútbol.");
                }
            } else if ("/cartelera".equalsIgnoreCase(comando)) {
                try {
                    List<TmdbMovie> movies = cinemaDataService.getTop10NowPlaying();

                    if (movies.isEmpty()) {
                        sendText(chatId, "🎬 No se ha podido obtener la cartelera en este momento. Inténtalo más tarde.");
                        return;
                    }

                    sendText(chatId, cinemaDataService.buildCarteleraMessage(movies));

                } catch (Exception e) {
                    e.printStackTrace();
                    sendText(chatId, "❌ Error al obtener la cartelera de cine.");
                }
            }

        }
    }

    private void sendText(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}