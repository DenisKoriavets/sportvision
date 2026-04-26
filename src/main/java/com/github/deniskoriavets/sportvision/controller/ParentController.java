package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.TelegramLinkRequest;
import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.request.ParentUpdateRequest;
import com.github.deniskoriavets.sportvision.service.interfaces.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@Tag(name = "Parents", description = "Managing parent profile")
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ParentResponse> getCurrentParent() {
        return ResponseEntity.ok(parentService.getCurrentParent());
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ParentResponse> updateParent(@Valid @RequestBody ParentUpdateRequest request) {
        return ResponseEntity.ok(parentService.updateCurrentParent(request));
    }

    @PostMapping("/me/telegram/link")
    @Operation(summary = "Link Telegram chat for notifications")
    public ResponseEntity<Void> linkTelegram(
        @Valid @RequestBody TelegramLinkRequest request) {
        parentService.linkTelegram(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all parents (admin only)")
    public ResponseEntity<Page<ParentResponse>> getAllParents(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(parentService.getAllParents(pageable));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate parent account (admin only)")
    public ResponseEntity<Void> deactivateParent(@PathVariable UUID id) {
        parentService.deactivateParent(id);
        return ResponseEntity.noContent().build();
    }
}