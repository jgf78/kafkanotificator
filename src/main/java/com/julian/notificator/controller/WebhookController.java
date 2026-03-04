package com.julian.notificator.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.model.webhook.NewsWebhookRequest;
import com.julian.notificator.service.WebhookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/webhook")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "200", description = "Request Successful"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
})
public class WebhookController {

    private final WebhookService service;

    @Value("${webhook.secret}")
    private String webhookSecret;

    @Operation(
            summary = "Receive News Webhook",
            operationId = "receiveWebhook",
            description = "Receives news from Make webhook",
            tags = { "Webhook API" }
    )
    @PostMapping("/input")
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestBody NewsWebhookRequest request) {

        if (!webhookSecret.equals(apiKey)) {
            return ResponseEntity.status(403).build();
        }

        service.setData(request);

        return ResponseEntity.ok().build();
    }
}