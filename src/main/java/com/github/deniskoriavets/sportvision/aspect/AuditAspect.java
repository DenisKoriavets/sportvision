package com.github.deniskoriavets.sportvision.aspect;

import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final SecurityFacade securityFacade;

    @AfterReturning(pointcut = "auditMethods()", returning = "result")
    public void logAuditAction(JoinPoint joinPoint, Object result) {
        var userId = securityFacade.getCurrentUserId();
        String actor = (userId != null) ? userId.toString() : "SYSTEM";

        var methodName = joinPoint.getSignature().getName();

        log.info("[AUDIT LOG] Actor: {} | Action: {} | Result: {}",
            actor,
            methodName,
            result);
    }

    @Pointcut("execution(* *..buySubscriptionManual(..)) || " +
        "execution(* *..markBulkAttendance(..)) || " +
        "execution(* *..initiatePayment(..))")
    public void auditMethods() {}
}
