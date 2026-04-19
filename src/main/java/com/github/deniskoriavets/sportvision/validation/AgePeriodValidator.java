package com.github.deniskoriavets.sportvision.validation;

import com.github.deniskoriavets.sportvision.dto.GroupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgePeriodValidator implements ConstraintValidator<ValidAgePeriod, GroupRequest> {

    @Override
    public boolean isValid(GroupRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;
        
        return request.ageMin() <= request.ageMax();
    }
}