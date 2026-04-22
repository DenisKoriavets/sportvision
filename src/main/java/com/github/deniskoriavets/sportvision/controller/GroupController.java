package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.GroupSearchCriteria;
import com.github.deniskoriavets.sportvision.service.interfaces.GroupService;
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
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Managing sport groups within sections")
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @Operation(summary = "Get groups with filtering and pagination")
    public ResponseEntity<Page<GroupResponse>> getGroups(
            @ParameterObject GroupSearchCriteria criteria,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(groupService.getGroups(criteria, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new group")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update group data")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable UUID id,
            @Valid @RequestBody GroupRequest request
    ) {
        return ResponseEntity.ok(groupService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete group")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}