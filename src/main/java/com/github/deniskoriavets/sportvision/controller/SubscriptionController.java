package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
@Tag(name = "Subscriptions", description = "Управління абонементами дітей (Тільки для батьків)")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Operation(summary = "Купити абонемент для дитини")
    @ApiResponse(responseCode = "201", description = "Абонемент успішно створено")
    @PostMapping("/buy")
    public ResponseEntity<SubscriptionResponse> buy(
        @RequestBody SubscriptionRequest subscriptionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subscriptionService.buySubscription(subscriptionRequest));
    }

    @Operation(summary = "Отримати всі абонементи конкретної дитини")
    @GetMapping("/child/{childId}")
    public ResponseEntity<List<SubscriptionResponse>> getByChildId(
        @PathVariable UUID childId) {
        return ResponseEntity.ok(subscriptionService.getChildSubscriptions(childId));
    }

    @Operation(summary = "Отримати деталі абонемента за ID")
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getById(
        @Parameter(description = "ID абонемента") @PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @Operation(summary = "Скасувати абонемент")
    @ApiResponse(responseCode = "204", description = "Абонемент скасовано")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.noContent().build();
    }
}
