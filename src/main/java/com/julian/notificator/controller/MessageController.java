package com.julian.notificator.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.service.KafkaProducerService;
import com.julian.notificator.service.impl.alexa.AlexaServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/messages")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
        @ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
        @ApiResponse(responseCode = "403", description = "Forbidden") })

public class MessageController {

    private final KafkaProducerService kafkaProducerService;
    private final AlexaServiceImpl alexaMessageService;

    public MessageController(KafkaProducerService kafkaProducerService, AlexaServiceImpl alexaMessageService) {
        this.kafkaProducerService = kafkaProducerService;
        this.alexaMessageService = alexaMessageService;
    }

    @Operation(summary = "Send Message", operationId = "sendMessage", description = "Send Message", tags = {
            "Messages API", })
    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageRequest request) {
        kafkaProducerService.sendMessage(request.getMessage(), request.getDestination());
        return "Mensaje enviado a " + request.getDestination() + ": " + request.getMessage();
    }
    
    @Operation(summary = "Read last message", operationId = "getLatestMessage", description = "Read last message", tags = {
            "Messages API", })
    @GetMapping("/latest")
    public Map<String, String> getLatestMessage() {
        String mensaje = alexaMessageService.getLastMessage();
        return Map.of("message", mensaje);
    }
}
