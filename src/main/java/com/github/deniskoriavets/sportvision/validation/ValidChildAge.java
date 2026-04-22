package com.github.deniskoriavets.sportvision.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ChildAgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChildAge {

    String message() default "Child must be between 4 and 16 years old";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
