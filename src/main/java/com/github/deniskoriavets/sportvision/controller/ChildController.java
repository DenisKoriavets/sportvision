package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.dto.request.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.response.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.ChildSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.ChildService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Children", description = "Управління даними дітей")
public class ChildController {

    private final ChildService childService;

    @PostMapping
    @Operation(summary = "Додати нову дитину")
    public ResponseEntity<ChildResponse> addChild(@Valid @RequestBody ChildRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(childService.createChild(request));
    }

    @GetMapping
    @Operation(summary = "Отримати список дітей поточного користувача")
    public ResponseEntity<List<ChildResponse>> getAllChildren() {
        return ResponseEntity.ok(childService.getAllChildrenForCurrentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати дитину за ID")
    public ResponseEntity<ChildResponse> getChildById(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити дані дитини")
    public ResponseEntity<ChildResponse> updateChild(@PathVariable UUID id,
                                                     @Valid @RequestBody ChildRequest request) {
        return ResponseEntity.ok(childService.updateChild(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити дитину")
    public ResponseEntity<Void> deleteChild(@PathVariable UUID id) {
        childService.deleteChild(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Пошук дітей за ПІБ, групою або батьком")
    public ResponseEntity<Page<ChildResponse>> searchChildren(
        ChildSearchCriteria criteria,
        Pageable pageable) {
        return ResponseEntity.ok(childService.searchChildren(criteria, pageable));
    }

    @GetMapping("/{id}/attendance")
    @Operation(summary = "Отримати історію відвідувань дитини")
    @PreAuthorize("hasAnyRole('PARENT', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getChildAttendance(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildAttendance(id));
    }

    @GetMapping("/{id}/subscriptions")
    @Operation(summary = "Отримати всі абонементи дитини")
    @PreAuthorize("hasAnyRole('PARENT', 'ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getChildSubscriptions(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildSubscriptions(id));
    }
}