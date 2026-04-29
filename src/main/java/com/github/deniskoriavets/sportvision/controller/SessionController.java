package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.ChildAttendanceDto;
import com.github.deniskoriavets.sportvision.dto.criteria.SessionSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.dto.request.SessionGenerationRequest;
import com.github.deniskoriavets.sportvision.dto.request.SessionRequest;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.dto.response.SessionResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.AttendanceService;
import com.github.deniskoriavets.sportvision.service.interfaces.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions & Attendance", description = "Managing training sessions and attendance")
public class SessionController {

    private final SessionService sessionService;
    private final AttendanceService attendanceService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate sessions from schedule for a date range")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid dates or request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> generateSessions(@Valid @RequestBody SessionGenerationRequest request) {
        sessionService.generateSessions(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create a one-off session manually")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Session created successfully",
            content = @Content(schema = @Schema(implementation = SessionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - only ADMIN or COACH",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SessionResponse> createManualSession(@Valid @RequestBody SessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createManualSession(request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Cancel a session with a reason")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Session cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - only ADMIN or COACH",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> cancelSession(@PathVariable UUID id, @RequestParam String reason) {
        sessionService.cancelSession(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sessions for a group within a date range")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of sessions returned",
            content = @Content(schema = @Schema(implementation = SessionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<SessionResponse>> getGroupSessions(
        @PathVariable UUID groupId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(sessionService.getSessionsByGroup(groupId, start, end));
    }

    @PostMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Mark bulk attendance for a session")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance marked successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate attendance",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - only COACH or ADMIN",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session or child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> markAttendance(
        @PathVariable UUID id,
        @RequestBody @Valid List<ChildAttendanceDto> attendances) {
        attendanceService.markBulkAttendance(new BulkAttendanceRequest(id, attendances));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search and filter sessions with pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions found",
            content = @Content(schema = @Schema(implementation = SessionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<SessionResponse>> searchSessions(
        SessionSearchCriteria criteria,
        Pageable pageable) {
        return ResponseEntity.ok(sessionService.searchSessions(criteria, pageable));
    }

    @PutMapping("/{sessionId}/attendance/{childId}")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Update attendance status for a specific child in a session")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance updated",
            content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Session is not in SCHEDULED status",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session, child or attendance record not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AttendanceResponse> updateAttendance(
        @PathVariable UUID sessionId,
        @PathVariable UUID childId,
        @RequestParam AttendanceStatus status) {
        return ResponseEntity.ok(
            attendanceService.updateAttendanceStatus(sessionId, childId, status));
    }
}
