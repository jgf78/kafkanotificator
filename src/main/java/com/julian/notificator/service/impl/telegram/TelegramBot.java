package com.julian.notificator.service.impl.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.julian.notificator.model.football.LiveMatchResponse;
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

    public TelegramBot(RestTemplate restTemplate, FootballDataService footballDataService) {
        this.restTemplate = restTemplate;
        this.footballDataService = footballDataService;
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
                    LiveMatchResponse resultLive = restTemplate.getForObject(backendUrlFootball, LiveMatchResponse.class);
                    String resultLiveFormatted = footballDataService.formatLiveMatchMessage(resultLive.getData());
                    sendText(chatId, resultLive != null ? resultLiveFormatted : "No se ha podido obtener el resultado.");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendText(chatId, "Error al obtener el resultado de fútbol.");
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