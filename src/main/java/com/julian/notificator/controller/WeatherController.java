package com.julian.notificator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.model.weather.WeeklyWeather;
import com.julian.notificator.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/weather")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
        @ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
        @ApiResponse(responseCode = "403", description = "Forbidden") })
public class WeatherController {
    private final WeatherService service;

    public WeatherController(WeatherService service) {
        this.service = service;
    }

    @Operation(summary = "Get weather by city", operationId = "getLatestMovieReleases", description = "Get weather by city", tags = {
            "Weather API", })
    @GetMapping
    public ResponseEntity<String> getWeatherByCity(@Parameter(
            description = "city",
            example = "Madrid"
        )
        @RequestParam String city) {        
        WeeklyWeather weeklyForecast = service.getWeeklyForecast(city);
        if(weeklyForecast!=null) return ResponseEntity.ok(service.formatWeeklyWeather(weeklyForecast));
        else return ResponseEntity.ok("");
    }
}
