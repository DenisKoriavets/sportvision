package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
@Tag(name = "Subscriptions", description = "Managing child subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Admin only: Purchase a subscription using cash")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/buy")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subscription purchased successfully",
            content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child or subscription plan not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Child already has active subscription for this section",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SubscriptionResponse> buyForCash(
        @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subscriptionService.buySubscriptionManual(request));
    }

    @Operation(summary = "Get all subscriptions for a child")
    @GetMapping("/child/{childId}")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of subscriptions returned",
            content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your child",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<SubscriptionResponse>> getByChildId(
        @PathVariable UUID childId) {
        return ResponseEntity.ok(subscriptionService.getChildSubscriptions(childId));
    }

    @Operation(summary = "Get subscription by ID")
    @GetMapping("/{id}")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription found",
            content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your subscription",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SubscriptionResponse> getById(
        @Parameter(description = "ID абонемента") @PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @Operation(summary = "Cancel a subscription")
    @PutMapping("/{id}/cancel")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subscription cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your subscription",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Cannot cancel subscription in current status",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.noContent().build();
    }
}