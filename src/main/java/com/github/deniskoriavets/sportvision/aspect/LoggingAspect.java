package com.github.deniskoriavets.sportvision.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        var args = joinPoint.getArgs();
        log.info("{} executed in {} ms | Args: {} | Result: {}",
            methodName,
            (end - start),
            java.util.Arrays.toString(args),
            result);
        return result;
    }

    @Pointcut("execution(* com.github.deniskoriavets.sportvision.service..*.*(..))")
    public void serviceMethods() {}

    @Pointcut("execution(* com.github.deniskoriavets.sportvision.controller..*.*(..))")
    public void controllerMethods() {}
}
