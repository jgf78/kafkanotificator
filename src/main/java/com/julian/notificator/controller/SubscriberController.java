package com.julian.notificator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian.notificator.entity.Subscribers;
import com.julian.notificator.model.subscriber.SubscribeRequest;
import com.julian.notificator.model.subscriber.TestNotificationRequest;
import com.julian.notificator.service.SubscriberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/subscribers")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request."),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
        @ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
        @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscriberService subscriberService;

    @Operation(summary = "Create new subscription (webhook)", operationId = "subscribe", description = "Create new subscription (webhook)", tags = {
            "Subscriber API", })
    @PostMapping("/subscribe")
    public ResponseEntity<Subscribers> subscribe(@RequestBody SubscribeRequest request) {

        Subscribers subscriber = subscriberService.subscribe(
                request.getName(),
                request.getCallbackUrl(),
                request.getEvents()
        );

        return ResponseEntity.ok(subscriber);
    }

    @Operation(summary = "List active subscribers", operationId = "getActiveSubscribers", description = "List active subscribers", tags = {
            "Subscriber API", })
    @GetMapping("/active")
    public ResponseEntity<List<Subscribers>> getActiveSubscribers() {
        return ResponseEntity.ok(subscriberService.getActiveSubscribers());
    }

    @Operation(summary = "Manually deactivate a subscriber", operationId = "deactivate", description = "Manually deactivate a subscriber", tags = {
            "Subscriber API", })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        subscriberService.deactivateSubscriber(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Test endpoint for sending manual notification", operationId = "sendTestNotification", description = "Test endpoint for sending manual notification", tags = {
            "Subscriber API", })
    @PostMapping("/test-notification")
    public ResponseEntity<Void> sendTestNotification(@RequestBody TestNotificationRequest request) {

        subscriberService.notifyAllSubscribers(
                request.getEventType(),
                request.getData()
        );

        return ResponseEntity.ok().build();
    }
}

