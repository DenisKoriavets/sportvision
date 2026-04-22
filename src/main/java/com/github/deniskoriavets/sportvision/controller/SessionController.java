package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.*;
import com.github.deniskoriavets.sportvision.dto.criteria.SessionSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.dto.request.SessionGenerationRequest;
import com.github.deniskoriavets.sportvision.dto.request.SessionRequest;
import com.github.deniskoriavets.sportvision.dto.response.SessionResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.AttendanceService;
import com.github.deniskoriavets.sportvision.service.interfaces.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
    public ResponseEntity<Void> generateSessions(@Valid @RequestBody
                                                 SessionGenerationRequest request) {
        sessionService.generateSessions(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create a one-off session manually")
    public ResponseEntity<SessionResponse> createManualSession(@Valid @RequestBody
                                                               SessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createManualSession(request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Cancel a session with a reason")
    public ResponseEntity<Void> cancelSession(@PathVariable UUID id, @RequestParam String reason) {
        sessionService.cancelSession(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get sessions for a group within a date range")
    public ResponseEntity<List<SessionResponse>> getGroupSessions(
            @PathVariable UUID groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(sessionService.getSessionsByGroup(groupId, start, end));
    }

    @PostMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Mark bulk attendance for a session")
    public ResponseEntity<Void> markAttendance(
            @PathVariable UUID id,
            @RequestBody @Valid List<ChildAttendanceDto> attendances) {
        attendanceService.markBulkAttendance(new BulkAttendanceRequest(id, attendances));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Search and filter sessions with pagination")
    public ResponseEntity<Page<SessionResponse>> searchSessions(
        SessionSearchCriteria criteria,
        Pageable pageable) {
        return ResponseEntity.ok(sessionService.searchSessions(criteria, pageable));
    }
}