package com.julian.notificator.service.impl.lottery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julian.notificator.model.lottery.LotteryResponse;
import com.julian.notificator.service.LotteryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {

    @Value("${lottery.base-url}")
    private String baseUrl;
    
    @Value("${lottery.token}")
    private String token;
    
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
    


}