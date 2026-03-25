package com.julian.notificator.service.impl.crypto;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.crypto.CryptoRecord;
import com.julian.notificator.service.CryptoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoServiceImpl implements CryptoService {

    private final RestTemplate restTemplate;

    private static final String URL =
            "https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&per_page=5&page=1";

    // =========================
    // 🔹 Método 1: llamada API
    // =========================
    @Override
    public List<CryptoRecord> getTop5Cryptos() {

        try {
            return restTemplate.exchange(
                    URL,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CryptoRecord>>() {}
            ).getBody();

        } catch (Exception e) {
            log.error("Error calling CoinGecko API", e);
            throw new RuntimeException("Error retrieving crypto data");
        }
    }

    // =========================
    // 🔹 Método 2: mensaje Telegram
    // =========================
    @Override
    public String buildTelegramMessage(List<CryptoRecord> cryptos) {

        if (cryptos == null || cryptos.isEmpty()) {
            return "⚠️ No hay datos de criptomonedas disponibles";
        }

        StringBuilder sb = new StringBuilder();
        DecimalFormat priceFormat = new DecimalFormat("#,###.##");
        DecimalFormat percentFormat = new DecimalFormat("#0.00");

        sb.append("🚀 *TOP 5 CRIPTOMONEDAS (EUR)*\n\n");

        for (int i = 0; i < cryptos.size(); i++) {

            CryptoRecord c = cryptos.get(i);

            String medal = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> "🔹";
            };

            String trend = c.change24h() >= 0 ? "🟢" : "🔻";

            sb.append(medal).append(" *")
              .append(c.name()).append(" (").append(c.symbol().toUpperCase()).append(")*\n")
              .append("💶 Precio: ").append(priceFormat.format(c.price())).append(" €\n")
              .append("📈 24h: ").append(percentFormat.format(c.change24h())).append("% ").append(trend).append("\n")
              .append("🏦 Market Cap: ").append(formatMarketCap(c.marketCap())).append("\n\n");
        }

        sb.append("━━━━━━━━━━━━━━\n");
        sb.append("⏱ Actualizado: ")
          .append(LocalTime.now(ZoneId.of("Europe/Madrid")).format(DateTimeFormatter.ofPattern("HH:mm")));

        return sb.toString();
    }

    // =========================
    // 🔹 Helper: MarketCap bonito
    // =========================
    private String formatMarketCap(long marketCap) {
        double value = marketCap;

        if (value >= 1_000_000_000) {
            return String.format("%.1fB €", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("%.1fM €", value / 1_000_000);
        } else {
            return String.format("%,.0f €", value);
        }
    }
}
