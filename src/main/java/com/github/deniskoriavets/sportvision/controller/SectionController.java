package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.SectionRequest;
import com.github.deniskoriavets.sportvision.dto.response.SectionResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.SectionSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Sections", description = "Managing sport sections")
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    @Operation(summary = "Get sections with filtering and pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of sections returned",
            content = @Content(schema = @Schema(implementation = SectionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<SectionResponse>> getSections(
        @ParameterObject SectionSearchCriteria criteria,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(sectionService.getSections(criteria, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new section")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Section created successfully",
            content = @Content(schema = @Schema(implementation = SectionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.createSection(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get section by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section found",
            content = @Content(schema = @Schema(implementation = SectionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Section not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SectionResponse> getSectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update section data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section updated successfully",
            content = @Content(schema = @Schema(implementation = SectionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Section not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SectionResponse> updateSection(
        @PathVariable UUID id,
        @Valid @RequestBody SectionRequest request
    ) {
        return ResponseEntity.ok(sectionService.updateSection(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete section")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Section deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Section not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSection(@PathVariable UUID id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}