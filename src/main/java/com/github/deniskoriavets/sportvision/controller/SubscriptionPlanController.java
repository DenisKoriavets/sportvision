package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.SubscriptionPlanRequest;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionPlanResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.SubscriptionPlanSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "Managing subscription plans for sections")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    @Operation(summary = "Get all available subscription plans")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of subscription plans returned",
            content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<SubscriptionPlanResponse>> getSubscriptionPlans(
        @ParameterObject SubscriptionPlanSearchCriteria searchCriteria,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(
            subscriptionPlanService.getAllSubscriptionPlans(searchCriteria, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription plan by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription plan found",
            content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class))),
        @ApiResponse(responseCode = "404", description = "Subscription plan not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SubscriptionPlanResponse> getSubscriptionPlanById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(subscriptionPlanService.getSubscriptionPlanById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new subscription plan")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subscription plan created successfully",
            content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error (including subscription plan rules)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SubscriptionPlanResponse> createSubscriptionPlan(@RequestBody @Valid SubscriptionPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subscriptionPlanService.createSubscriptionPlan(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a subscription plan")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription plan updated successfully",
            content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Subscription plan not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SubscriptionPlanResponse> updateSubscriptionPlan(
        @PathVariable("id") UUID id,
        @RequestBody @Valid SubscriptionPlanRequest request
    ) {
        return ResponseEntity.ok(subscriptionPlanService.updateSubscriptionPlan(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a subscription plan")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subscription plan deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Subscription plan not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSubscriptionPlan(@PathVariable("id") UUID id) {
        subscriptionPlanService.deleteSubscriptionPlan(id);
        return ResponseEntity.noContent().build();
    }
}