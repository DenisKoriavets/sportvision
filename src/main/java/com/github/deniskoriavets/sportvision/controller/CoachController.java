package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.GroupService;
import com.github.deniskoriavets.sportvision.service.interfaces.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
@Tag(name = "Coaches", description = "Managing coaches")
public class CoachController {

    private final ParentService parentService;
    private final GroupService groupService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all coaches")
    public ResponseEntity<Page<ParentResponse>> getAllCoaches(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(parentService.getAllCoaches(pageable));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign coach role to a parent")
    public ResponseEntity<Void> assignCoach(@PathVariable UUID id) {
        parentService.assignCoachRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/groups")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Get groups assigned to a coach")
    public ResponseEntity<List<GroupResponse>> getCoachGroups(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getGroupsByCoachId(id));
    }
}