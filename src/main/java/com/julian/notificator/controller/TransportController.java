package com.julian.notificator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.model.transport.TelegramStop;
import com.julian.notificator.service.TransportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transports")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "200", description = "Request Successful"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
})
public class TransportController {

    private final TransportService transportService;

    @Operation(
            summary = "Get public transport near you",
            operationId = "getStopsNearby",
            description = "Get public transport near you",
            tags = { "Transports API" }
    )
    @PostMapping("/nearby")
    public ResponseEntity<List<TelegramStop>> receiveWebhook(
            @RequestParam String latitude,
            @RequestParam String longitude) {

        List<TelegramStop> stops = transportService.getStopsNearby(latitude, longitude, 200);
        return ResponseEntity.ok(stops);
    }
}