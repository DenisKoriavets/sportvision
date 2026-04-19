package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.ScheduleRequest;
import com.github.deniskoriavets.sportvision.dto.ScheduleResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Schedules", description = "Управління шаблонами розкладу груп")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити новий слот у розкладі")
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(request));
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Отримати весь розклад конкретної групи")
    public ResponseEntity<List<ScheduleResponse>> getGroupSchedules(@PathVariable UUID groupId) {
        return ResponseEntity.ok(scheduleService.getGroupSchedules(groupId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити слот із розкладу")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}