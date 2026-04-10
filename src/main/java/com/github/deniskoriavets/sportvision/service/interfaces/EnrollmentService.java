package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.EnrollmentRequest;

public interface EnrollmentService {
    void enrollChild(EnrollmentRequest enrollmentRequest);

    void unenrollChild(EnrollmentRequest enrollmentRequest);
}
