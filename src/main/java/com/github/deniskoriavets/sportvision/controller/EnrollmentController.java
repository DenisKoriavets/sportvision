package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.EnrollmentRequest;
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
@Tag(name = "Enrollments", description = "Управління записом дітей у спортивні групи (Тільки для PARENT)")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    @Operation(
        summary = "Записати дитину в групу",
        description = "Перевіряє вік дитини, місткість групи та наявність активного абонемента на відповідну секцію."
    )
    public ResponseEntity<Void> enrollChildInSection(@RequestBody
                                                     EnrollmentRequest enrollmentRequest) {
        enrollmentService.enrollChild(enrollmentRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unenroll")
    @Operation(
        summary = "Відрахувати дитину з групи",
        description = "Видаляє зв'язок між дитиною та групою (soft delete). Абонемент залишається активним."
    )
    public ResponseEntity<Void> unenrollChildFromSection(@RequestBody
                                                         EnrollmentRequest enrollmentRequest) {
        enrollmentService.unenrollChild(enrollmentRequest);
        return ResponseEntity.noContent().build();
    }
}
