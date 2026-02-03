package com.julian.notificator.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {

    private String proxyUrl;
    private String chatId;
    private List<String> chatIdsGroups;
    private String botToken;
    private String botUsername;
    private String backendUrl;
    private String backendUrl2;

}

