package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.GroupService;
import com.github.deniskoriavets.sportvision.service.interfaces.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of coaches returned",
            content = @Content(schema = @Schema(implementation = ParentResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ParentResponse>> getAllCoaches(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(parentService.getAllCoaches(pageable));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign coach role to a parent")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Coach role assigned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Parent not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> assignCoach(@PathVariable UUID id) {
        parentService.assignCoachRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/groups")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Get groups assigned to a coach")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of groups returned",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - only coach or admin can view",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Coach not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<GroupResponse>> getCoachGroups(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getGroupsByCoachId(id));
    }

    @DeleteMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke COACH role, downgrade back to PARENT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Coach role revoked successfully"),
        @ApiResponse(responseCode = "400", description = "Parent is not a coach",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Parent not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> revokeCoach(@PathVariable UUID id) {
        parentService.revokeCoachRole(id);
        return ResponseEntity.noContent().build();
    }
}
