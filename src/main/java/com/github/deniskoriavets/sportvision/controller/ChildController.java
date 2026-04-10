package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.ChildResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.ChildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}