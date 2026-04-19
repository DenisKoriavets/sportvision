package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanResponse;
import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanSearchCriteria;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "Управління тарифними планами секцій")
public class SubscriptionPlanController {
    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    @Operation(summary = "Отримати всі доступні тарифні плани")
    public ResponseEntity<Page<SubscriptionPlanResponse>> getSubscriptionPlans(@ParameterObject
                                                                               SubscriptionPlanSearchCriteria searchCriteria,
                                                                               @ParameterObject
                                                                               Pageable pageable) {
        return ResponseEntity.ok(
            subscriptionPlanService.getAllSubscriptionPlans(searchCriteria, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати тарифний план за ID")
    public ResponseEntity<SubscriptionPlanResponse> getSubscriptionPlanById(@PathVariable("id")
                                                                            UUID id) {
        return ResponseEntity.ok(subscriptionPlanService.getSubscriptionPlanById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити новий тарифний план (Тільки для ADMIN)")
    public ResponseEntity<SubscriptionPlanResponse> createSubscriptionPlan(@RequestBody
                                                                           @Valid
                                                                           SubscriptionPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subscriptionPlanService.createSubscriptionPlan(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Оновити існуючий тарифний план (Тільки для ADMIN)")
    public ResponseEntity<SubscriptionPlanResponse> updateSubscriptionPlan(
        @PathVariable("id") UUID id,
        @RequestBody @Valid SubscriptionPlanRequest request
    ) {
        return ResponseEntity.ok(subscriptionPlanService.updateSubscriptionPlan(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити тарифний план (Тільки для ADMIN)")
    public ResponseEntity<Void> deleteSubscriptionPlan(@PathVariable("id") UUID id) {
        subscriptionPlanService.deleteSubscriptionPlan(id);
        return ResponseEntity.noContent().build();
    }
}
