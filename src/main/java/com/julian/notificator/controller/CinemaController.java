package com.julian.notificator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.model.cinema.TmdbMovie;
import com.julian.notificator.service.CinemaDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/cinema")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
        @ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
        @ApiResponse(responseCode = "403", description = "Forbidden") })
public class CinemaController {
    private final CinemaDataService service;

    public CinemaController(CinemaDataService service) {
        this.service = service;
    }

    @Operation(summary = "Get the latest movie releases", operationId = "getLatestMovieReleases", description = "Get the latest movie releases", tags = {
            "Cinema API", })
    @GetMapping("/movies")
    public ResponseEntity<List<TmdbMovie>> getLatestMovieReleases() {
        return ResponseEntity.ok(service.getTop10NowPlaying());
    }
}
