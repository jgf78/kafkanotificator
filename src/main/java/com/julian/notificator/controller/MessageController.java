package com.julian.notificator.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.julian.notificator.model.DestinationType;
import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.TelegramPollRequest;
import com.julian.notificator.service.KafkaProducerService;
import com.julian.notificator.service.impl.alexa.AlexaServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
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

    @Operation(summary = "Send Message", operationId = "sendMessage", description = "Send Message", tags = {
            "Messages API", })
    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageRequest request) {
        kafkaProducerService.sendMessage(request.getMessage(), request.getDestination(), request.getThreadId());
        return "Mensaje enviado a " + request.getDestination() + ": " + request.getMessage();
    }
    
    @Operation(summary = "Send Pin Message", operationId = "sendPinMessage", description = "Send Pin Message", tags = {
            "Messages API", })
    @PostMapping("/sendPin")
    public String sendPinMessage(@RequestParam("message") String pinMessage) {
        kafkaProducerService.sendPinMessage(pinMessage);
        return "Mensaje enviado y anclado: " + pinMessage;
    }
    
    @Operation(summary = "Send Poll", operationId = "sendPoll", description = "Send Poll", tags = {
            "Messages API", })
    @PostMapping("/sendPoll")
    public String sendPoll(@RequestBody TelegramPollRequest request) {
        kafkaProducerService.sendPoll(request);
        return "Encuesta enviada ✅";
    }

    @Operation(summary = "Send message with document", description = "Send message with document", tags = {
            "Messages API", })
    @PostMapping("/sendFile")
    public String sendFile(
            @RequestParam("message") String message,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "filename", required = false) String filename,
            @Parameter(
                    description = "Destination of the message. Allowed values: DISCORD, TELEGRAM, MAIL",
                    required = true,
                    schema = @Schema(type = "string", allowableValues = {"DISCORD", "TELEGRAM", "MAIL"})
                )
            @RequestParam(value = "destination", required = true) String destinationStr) {
        try {
            DestinationType destination = DestinationType.valueOf(destinationStr.toUpperCase());
            kafkaProducerService.sendFile(message, file, filename, destination);
            return "Mensaje enviado con éxito: " + message;
        } catch (Exception e) {
            log.error("Error enviando mensaje: ", e);
            return "Error enviando mensaje: " + e.getMessage();
        }
    }

    @Operation(summary = "Read last message", operationId = "getLatestMessage", description = "Read last message", tags = {
            "Messages API", })
    @GetMapping("/latest")
    public Map<String, String> getLatestMessage() {
        String mensaje = alexaMessageService.getLastMessage();
        return Map.of("message", mensaje);
    }
}
