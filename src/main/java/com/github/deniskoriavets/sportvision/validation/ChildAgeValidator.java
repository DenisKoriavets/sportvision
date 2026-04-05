package com.github.deniskoriavets.sportvision.validation;

import jakarta.validation.ConstraintValidator;
import java.time.LocalDate;
import java.time.Period;

public class ChildAgeValidator implements ConstraintValidator<ValidChildAge, LocalDate> {

    @Override
    public boolean isValid(LocalDate birthDate, jakarta.validation.ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true;
        }
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();
        return age >= 4 && age <= 16;
    }
}
