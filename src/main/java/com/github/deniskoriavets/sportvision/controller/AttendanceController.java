package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.dto.ChildAttendanceDto;
import com.github.deniskoriavets.sportvision.service.interfaces.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions & Attendance", description = "Управління заняттями та відмітка відвідуваності")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(
        summary = "Відмітити відвідування (масово)",
        description = "Дозволяє тренеру або адміну проставити статуси всім дітям на конкретному занятті. " +
                      "Для статусу PRESENT автоматично списується заняття з абонемента."
    )
    @ApiResponse(responseCode = "200", description = "Відвідування успішно відмічено")
    @ApiResponse(responseCode = "403", description = "Недостатньо прав (потрібна роль COACH або ADMIN)")
    @ApiResponse(responseCode = "404", description = "Заняття не знайдено")
    @PostMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    public ResponseEntity<Void> markAttendance(
        @PathVariable UUID id,
        @RequestBody @Valid List<ChildAttendanceDto> attendances) {
        
        var bulkRequest = new BulkAttendanceRequest(id, attendances);
        attendanceService.markBulkAttendance(bulkRequest);
        
        return ResponseEntity.ok().build();
    }
}