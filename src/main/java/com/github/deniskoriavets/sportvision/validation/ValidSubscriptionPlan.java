package com.github.deniskoriavets.sportvision.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SubscriptionPlanValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSubscriptionPlan {

    String message() default "Невірна комбінація полів: якщо isUnlimited=false, то sessionsCount має бути задано і бути додатнім числом";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
