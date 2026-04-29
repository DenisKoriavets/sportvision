package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.TelegramLinkRequest;
import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.request.ParentUpdateRequest;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Parents", description = "Managing parent profile")
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current parent profile returned",
            content = @Content(schema = @Schema(implementation = ParentResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ParentResponse> getCurrentParent() {
        return ResponseEntity.ok(parentService.getCurrentParent());
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = ParentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ParentResponse> updateParent(@Valid @RequestBody ParentUpdateRequest request) {
        return ResponseEntity.ok(parentService.updateCurrentParent(request));
    }

    @PostMapping("/me/telegram/link")
    @Operation(summary = "Link Telegram chat for notifications")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Telegram chat linked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> linkTelegram(
        @Valid @RequestBody TelegramLinkRequest request) {
        parentService.linkTelegram(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all parents (admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of parents returned",
            content = @Content(schema = @Schema(implementation = ParentResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ParentResponse>> getAllParents(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(parentService.getAllParents(pageable));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate parent account (admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Parent account deactivated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Parent not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivateParent(@PathVariable UUID id) {
        parentService.deactivateParent(id);
        return ResponseEntity.noContent().build();
    }
}