package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.EnrollmentRequest;
import com.github.deniskoriavets.sportvision.service.interfaces.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Void> enrollChildInSection(@RequestBody
                                                     EnrollmentRequest enrollmentRequest) {
        enrollmentService.enrollChild(enrollmentRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(
        summary = "Unenroll a child from a group",
        description = "Removes the child-group link. The subscription remains active."
    )
    public ResponseEntity<Void> unenrollChildFromSection(@RequestBody
                                                         EnrollmentRequest enrollmentRequest) {
        enrollmentService.unenrollChild(enrollmentRequest);
        return ResponseEntity.noContent().build();
    }
}
