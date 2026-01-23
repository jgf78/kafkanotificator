package com.julian.notificator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.model.football.LiveMatchResponse;
import com.julian.notificator.service.FootballDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/football")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
        @ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
        @ApiResponse(responseCode = "403", description = "Forbidden") })
public class FootballController {
    private final FootballDataService service;

    public FootballController(FootballDataService service) {
        this.service = service;
    }

    @Operation(summary = "Get live football scores", operationId = "getLiveMatch", description = "Get live football scores", tags = {
            "Football API", })
    @GetMapping("/live")
    public ResponseEntity<LiveMatchResponse> getLiveMatch() {
        return ResponseEntity.ok(service.getLiveStatus());
    }
}
