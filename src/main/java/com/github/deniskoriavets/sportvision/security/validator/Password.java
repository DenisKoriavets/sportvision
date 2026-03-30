package com.github.deniskoriavets.sportvision.security.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "Пароль повинен бути від 8 символів і містити хоча б одну цифру та одну велику літеру";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}