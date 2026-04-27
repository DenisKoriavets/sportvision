package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.ScheduleRequest;
import com.github.deniskoriavets.sportvision.dto.response.ScheduleResponse;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.ScheduleService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Managing recurring schedule templates for groups")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new schedule slot")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Schedule created successfully",
            content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(request));
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get full schedule for a group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Schedule for the group returned",
            content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ScheduleResponse>> getGroupSchedules(@PathVariable UUID groupId) {
        return ResponseEntity.ok(scheduleService.getGroupSchedules(groupId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a schedule slot")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Schedule deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Schedule not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}