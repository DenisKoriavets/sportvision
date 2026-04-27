package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.dto.request.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.response.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.ChildSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.ChildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
@Tag(name = "Children", description = "Managing children linked to a parent account")
public class ChildController {

    private final ChildService childService;

    @PostMapping
    @Operation(summary = "Add a new child")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Child created successfully",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed (age, name etc.)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ChildResponse> addChild(@Valid @RequestBody ChildRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(childService.createChild(request));
    }

    @GetMapping
    @Operation(summary = "Get all children of the current user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of children returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ChildResponse>> getAllChildren() {
        return ResponseEntity.ok(childService.getAllChildrenForCurrentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get child by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Child found",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - this child belongs to another parent",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ChildResponse> getChildById(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update child data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Child updated successfully",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not your child",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ChildResponse> updateChild(@PathVariable UUID id,
                                                     @Valid @RequestBody ChildRequest request) {
        return ResponseEntity.ok(childService.updateChild(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete child")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Child deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your child",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteChild(@PathVariable UUID id) {
        childService.deleteChild(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search children by name, group or parent")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ChildResponse>> searchChildren(
        ChildSearchCriteria criteria,
        Pageable pageable) {
        return ResponseEntity.ok(childService.searchChildren(criteria, pageable));
    }

    @GetMapping("/{id}/attendance")
    @Operation(summary = "Get attendance history for a child")
    @PreAuthorize("hasAnyRole('PARENT', 'ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance history retrieved",
            content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<AttendanceResponse>> getChildAttendance(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildAttendance(id));
    }

    @GetMapping("/{id}/subscriptions")
    @Operation(summary = "Get all subscriptions for a child")
    @PreAuthorize("hasAnyRole('PARENT', 'ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscriptions retrieved",
            content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<SubscriptionResponse>> getChildSubscriptions(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildSubscriptions(id));
    }
}