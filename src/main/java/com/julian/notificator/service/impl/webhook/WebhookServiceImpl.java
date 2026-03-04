package com.julian.notificator.service.impl.webhook;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.julian.notificator.model.webhook.NewsWebhookRequest;
import com.julian.notificator.service.NotificationService;
import com.julian.notificator.service.WebhookService;
import com.julian.notificator.service.util.UtilString;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebhookServiceImpl implements WebhookService {

    private final NotificationService telegramService;
    
    public WebhookServiceImpl(
            @Qualifier("telegramServiceImpl") NotificationService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void setData(NewsWebhookRequest request) {

        log.info("Nueva noticia recibida: {}", request.title());

        //boolean breaking = isBreakingNews(request);

        String message = buildTelegramMessage(request, true);

        telegramService.sendMessage(message);
    }

    private boolean isBreakingNews(NewsWebhookRequest request) {

        String title = request.title() != null
                ? request.title().toLowerCase(Locale.ROOT)
                : "";

        return title.toLowerCase().contains("última hora")
                || title.toLowerCase().contains("urgente")
                || title.toLowerCase().contains("breaking");
    }

    private String buildTelegramMessage(NewsWebhookRequest request, boolean breaking) {

        StringBuilder sb = new StringBuilder();

        if (breaking) {
            sb.append("🚨🚨 *ÚLTIMA HORA* 🚨🚨\n\n");
        } else {
            sb.append("📰 *Nueva noticia*\n\n");
        }

        sb.append("🗞 *").append(UtilString.escapeMarkdown(request.title())).append("*\n\n");

        if (request.summary() != null) {
            sb.append("📄 ").append(UtilString.escapeMarkdown(request.summary())).append("\n\n");
        }

        sb.append("🔗 ").append(request.link()).append("\n\n");

        if (request.source() != null) {
            sb.append("🏢 *Fuente:* ").append((UtilString.escapeMarkdown(request.source())));
        }

        return sb.toString();
    }

    /**
     * Muy importante para Telegram MarkdownV2
     */
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}