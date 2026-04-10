package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.SectionRequest;
import com.github.deniskoriavets.sportvision.dto.SectionResponse;
import com.github.deniskoriavets.sportvision.dto.SectionSearchCriteria;
import com.github.deniskoriavets.sportvision.service.interfaces.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@Tag(name = "Sections", description = "Управління спортивними секціями клубу")
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    @Operation(summary = "Отримати список секцій з фільтрацією та пагінацією")
    public ResponseEntity<Page<SectionResponse>> getSections(
            @ParameterObject SectionSearchCriteria criteria,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(sectionService.getSections(criteria, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити нову секцію (Тільки для ADMIN)")
    public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.createSection(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати секцію за ID")
    public ResponseEntity<SectionResponse> getSectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Оновити дані секції (Тільки для ADMIN)")
    public ResponseEntity<SectionResponse> updateSection(
            @PathVariable UUID id,
            @Valid @RequestBody SectionRequest request
    ) {
        return ResponseEntity.ok(sectionService.updateSection(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити секцію (Тільки для ADMIN)")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}