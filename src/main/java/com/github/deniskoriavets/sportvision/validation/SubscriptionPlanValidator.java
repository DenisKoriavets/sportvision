package com.github.deniskoriavets.sportvision.validation;

import com.github.deniskoriavets.sportvision.dto.request.SubscriptionPlanRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SubscriptionPlanValidator implements ConstraintValidator<ValidSubscriptionPlan, SubscriptionPlanRequest> {

    @Override
    public boolean isValid(SubscriptionPlanRequest subscriptionPlanRequest,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (subscriptionPlanRequest == null || subscriptionPlanRequest.isUnlimited() == null) {
            return true;
        }
        if (subscriptionPlanRequest.isUnlimited()) {
            return true;
        } else {
            return subscriptionPlanRequest.sessionsCount() != null && subscriptionPlanRequest.sessionsCount() > 0;
        }
    }
}
