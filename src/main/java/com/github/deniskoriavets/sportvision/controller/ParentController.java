package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.ParentUpdateRequest;
import com.github.deniskoriavets.sportvision.service.interfaces.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@Tag(name = "Parents", description = "Управління профілем батьків")
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/me")
    @Operation(summary = "Отримати профіль поточного користувача")
    public ResponseEntity<ParentResponse> getCurrentParent() {
        return ResponseEntity.ok(parentService.getCurrentParent());
    }

    @PutMapping("/me")
    @Operation(summary = "Оновити дані профілю")
    public ResponseEntity<ParentResponse> updateParent(@Valid @RequestBody ParentUpdateRequest request) {
        return ResponseEntity.ok(parentService.updateCurrentParent(request));
    }
}