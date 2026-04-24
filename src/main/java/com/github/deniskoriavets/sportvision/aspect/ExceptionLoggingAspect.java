package com.github.deniskoriavets.sportvision.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExceptionLoggingAspect {

    @AfterThrowing(value = "com.github.deniskoriavets.sportvision.aspect.LoggingAspect.controllerMethods()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("{} threw an exception: {}", methodName, ex.getMessage(), ex);
    }
}
