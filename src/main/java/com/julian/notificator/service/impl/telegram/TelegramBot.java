package com.julian.notificator.service.impl.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.julian.notificator.service.NewsService;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.bot-username}")
    private String botUsername;

    private final NewsService newsService;
    
    private final RestTemplate restTemplate;

    public TelegramBot(NewsService newsService) {
        this.newsService = newsService;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String texto = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if ("/titulares".equalsIgnoreCase(texto)) {
                try {
                    String titulares = newsService.getHeadlines();
                    sendText(chatId, titulares != null ? titulares : "No se han podido obtener los titulares.");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendText(chatId, "Error al obtener los titulares.");
                }

            } else {
                sendText(chatId, "Comando no reconocido.");
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
