package com.julian.notificator.service.impl.lottery;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.lottery.LotteryResponse;
import com.julian.notificator.model.lottery.LotteryResult;
import com.julian.notificator.model.lottery.ResultData;
import com.julian.notificator.service.LotteryService;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {

    @Value("${lottery.base-url}")
    private String baseUrl;
    
    @Value("${lottery.token}")
    private String token;
    
    @Value("${rss.proxy-url2}")
    private String rssProxyUrl;
    
    private final RestTemplate restTemplate;

    @Override
    public LotteryResponse getLatestResults() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<LotteryResponse> response = restTemplate.exchange(
                baseUrl + "/results",
                HttpMethod.GET,
                entity,
                LotteryResponse.class
        );

        return response.getBody();
    }

    @Override
    public String buildLotteryMessage(LotteryResponse response) throws IllegalArgumentException, FeedException, IOException {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            return "🎲 No hay resultados de loterías disponibles 😔";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🍀 *Últimos resultados de loterías* 💰\n");
        sb.append("────────────────────────────\n");

        for (LotteryResult result : response.data()) {
            String gameName = result.game() != null && result.game().name() != null
                    ? result.game().name()
                    : "Desconocido";

            // Emoji por tipo de lotería 
            String gameEmoji = switch (gameName.toLowerCase()) {
                case "lototurf" -> "🟣🎯";
                case "el quinto plus" -> "🟢🎲";
                case "el gordo" -> "🔴💸";
                case "bonoloto" -> "🔵🍀";
                case "la primitiva" -> "🟡💰";
                case "euromillones" -> "🌟💎";
                default -> "🎲";
            };

            sb.append(gameEmoji).append(" *").append(gameName).append("*\n");
            sb.append("📅 ").append(result.drawDate()).append(" (").append(result.dayOfWeek()).append(")\n");

            // Combinación
            if (result.combination() != null && !result.combination().isEmpty()) {
                sb.append("🔢 Combinación: ")
                  .append(result.combination().stream().map(String::valueOf).collect(Collectors.joining(" - ")))
                  .append("\n");
            }

            // Resultados especiales
            ResultData rd = result.resultData();
            if (rd != null) {
                if (rd.complementario() != null) sb.append("➕ Complementario: ").append(rd.complementario()).append("\n");
                if (rd.reintegro() != null) sb.append("🔄 Reintegro: ").append(rd.reintegro()).append("\n");
                if (rd.estrellas() != null && !rd.estrellas().isEmpty())
                    sb.append("⭐ Estrellas: ")
                      .append(rd.estrellas().stream().map(String::valueOf).collect(Collectors.joining(" - ")))
                      .append("\n");
                if (rd.joker() != null)
                    sb.append("🎰 Joker: ").append(rd.joker().combinacion() != null ? rd.joker().combinacion() : "-").append("\n");
            }

            // Jackpot
            if (result.jackpotFormatted() != null) {
                sb.append("💸 Bote: ").append(result.jackpotFormatted()).append("\n");
            }

            sb.append("────────────────────────────\n");
        }

        sb.append(getJuegosOnce());
        return sb.toString();
    }

    private String getJuegosOnce() throws IllegalArgumentException, FeedException, IOException {

        URL url = new URL(rssProxyUrl);

        SyndFeed feed;
        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(url)) {
            feed = input.build(reader);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🎲 *Resultados Juegos Once*\n\n");

        feed.getEntries()
            .stream()
            .limit(10)
            .forEach(entry -> {
                String title = entry.getTitle();
                String emoji = getEmojiForTitle(title);

                sb.append(emoji).append(" *").append(title).append("*\n")
                  .append(entry.getDescription().getValue()).append("\n")
                  .append("[Ver sorteo](").append(entry.getLink()).append(")\n\n");
            });

        return sb.toString();
    }

    private String getEmojiForTitle(String title) {
        title = title.toLowerCase();

        if (title.contains("cupón diario")) return "📅";
        if (title.contains("triplex")) return "🎲";
        if (title.contains("mi día")) return "⭐";
        if (title.contains("eurojackpot")) return "💰";
        if (title.contains("cuponazo")) return "🎯";
        if (title.contains("super 11") || title.contains("superonce")) return "🏆";
        if (title.contains("sueldazo")) return "💵";

        return "🎲"; 
    }


}
