package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.response.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.GroupSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.ChildService;
import com.github.deniskoriavets.sportvision.service.interfaces.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Managing sport groups within sections")
public class GroupController {

    private final GroupService groupService;
    private final ChildService childService;

    @GetMapping
    @Operation(summary = "Get groups with filtering and pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of groups returned",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<GroupResponse>> getGroups(
        @ParameterObject GroupSearchCriteria criteria,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(groupService.getGroups(criteria, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new group")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Group created successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error (including age period)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group found",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update group data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group updated successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GroupResponse> updateGroup(
        @PathVariable UUID id,
        @Valid @RequestBody GroupRequest request
    ) {
        return ResponseEntity.ok(groupService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete group")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Get all children in a group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of children in the group returned",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - only coach or admin",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ChildResponse>> getChildrenByGroup(@PathVariable UUID id) {
        return ResponseEntity.ok(childService.getChildrenByGroupId(id));
    }
}