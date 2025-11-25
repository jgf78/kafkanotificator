package com.julian.notificator.service.impl.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("whatsappServiceImpl")
public class WhatsappServiceImpl implements NotificationService {

    @Value("${whatsapp.apikey}")
    private String apikey;

    @Value("${whatsapp.type}")
    private String type;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String message) {
        try {
            String url = String.format("https://api.inout.bot/send?type=%s&apikey=%s&message=%s",
                    type, apikey, message);

            log.debug("WhatsappService - enviando mensaje a InOut.bot: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>("", headers);

            String response = restTemplate.postForObject(url, request, String.class);

            log.debug("WhatsappService - respuesta de InOut.bot: {}", response);
        } catch (Exception e) {
            log.error("WhatsappService - error enviando mensaje a WhatsApp", e);
        }
    }

    @Override
    public String getChannelName() {
        return "Whatsapp";
    }

    @Override
    public void sendMessageFile(MessagePayload payload) {
    }
    
}
