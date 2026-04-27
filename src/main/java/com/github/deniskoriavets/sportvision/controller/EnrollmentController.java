package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.EnrollmentRequest;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollments")
@PreAuthorize("hasRole('PARENT')")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Managing child enrollment in sport groups")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(
        summary = "Enroll a child in a group",
        description = "Validates child's age, group capacity and active subscription for the section."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Child successfully enrolled in the group"),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the owner of the child",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child or group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Business rule violation: age mismatch, group is full, or no active subscription",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> enrollChildInSection(
        @Valid @RequestBody EnrollmentRequest enrollmentRequest) {
        enrollmentService.enrollChild(enrollmentRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(
        summary = "Unenroll a child from a group",
        description = "Removes the child-group link. The subscription remains active."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Child successfully unenrolled from the group"),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the owner of the child",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child or group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> unenrollChildFromSection(
        @Valid @RequestBody EnrollmentRequest enrollmentRequest) {
        enrollmentService.unenrollChild(enrollmentRequest);
        return ResponseEntity.noContent().build();
    }
}