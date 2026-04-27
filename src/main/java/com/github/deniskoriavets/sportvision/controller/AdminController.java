package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.response.AdminStatsResponse;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin statistics and management")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @Operation(summary = "Get general system statistics")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = AdminStatsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}