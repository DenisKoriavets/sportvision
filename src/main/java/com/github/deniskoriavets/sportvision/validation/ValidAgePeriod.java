package com.github.deniskoriavets.sportvision.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgePeriodValidator.class)
public @interface ValidAgePeriod {
    String message() default "Minimum age cannot be greater than maximum age";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}