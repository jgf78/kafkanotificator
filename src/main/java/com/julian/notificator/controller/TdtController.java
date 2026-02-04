package com.julian.notificator.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.model.tdt.TdtProgramme;
import com.julian.notificator.model.tdt.TvNowResponse;
import com.julian.notificator.service.TdtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tv")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
        @ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
        @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequiredArgsConstructor
public class TdtController {

    private final TdtService tdtService;

    @Operation(summary = "Get the current TDT programming", operationId = "getTvNow", description = "Get the current TDT programming", tags = {
            "TDT API", })
    @GetMapping("/now")
    public List<TvNowResponse> getTvNow() {
        return tdtService.getTvNow()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TvNowResponse toResponse(TdtProgramme programme) {
        return TvNowResponse.builder()
                .channel(programme.getChannelId())
                .title(programme.getTitle())
                .start(programme.getStart())
                .stop(programme.getStop())
                .build();
    }
}

