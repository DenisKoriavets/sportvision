package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.criteria.ChildSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.AdminStatsResponse;
import com.github.deniskoriavets.sportvision.dto.response.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.AdminService;
import com.github.deniskoriavets.sportvision.service.interfaces.ChildService;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin statistics and management")
public class AdminController {

    private final AdminService adminService;
    private final ChildService childService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/stats")
    @Operation(summary = "Get general system statistics")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = AdminStatsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/children")
    @Operation(summary = "List all children in the system (admin view)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Children list returned",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ChildResponse>> getAllChildren(
        @ParameterObject ChildSearchCriteria criteria,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(childService.getAllChildrenAdmin(criteria, pageable));
    }

    @GetMapping("/children/{id}")
    @Operation(summary = "Get a child by id (admin view, no ownership check)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Child found",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ChildResponse> getChildById(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildByIdAdmin(id));
    }

    @DeleteMapping("/children/{id}")
    @Operation(summary = "Delete any child (soft delete, no ownership check)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Child deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteChild(@PathVariable UUID id) {
        childService.deleteChildAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "List all subscriptions in the system (admin view, optional status filter)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription list returned",
            content = @Content(schema = @Schema(implementation = SubscriptionResponse.class)))
    })
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions(
        @RequestParam(required = false) List<SubscriptionStatus> statuses) {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptionsAdmin(statuses));
    }

    @PutMapping("/subscriptions/{id}/activate")
    @Operation(summary = "Manually activate a PENDING_PAYMENT subscription (e.g. cash payment)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription activated",
            content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Subscription is not in PENDING_PAYMENT status",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SubscriptionResponse> activateSubscription(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.activateSubscriptionAdmin(id));
    }
}
