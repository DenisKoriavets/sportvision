package com.github.deniskoriavets.sportvision.aspect;

import java.util.Arrays;
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
        String methodName = joinPoint.getSignature().toShortString();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("{} executed in {} ms | Args: {} | Result: {}",
                methodName, elapsed, Arrays.toString(joinPoint.getArgs()), result);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("{} failed after {} ms | Args: {} | Error: {}",
                methodName, elapsed, Arrays.toString(joinPoint.getArgs()), ex.getMessage());
            throw ex;
        }
    }

    @Pointcut("execution(* com.github.deniskoriavets.sportvision.service..*.*(..))")
    public void serviceMethods() {}

    @Pointcut("execution(* com.github.deniskoriavets.sportvision.controller..*.*(..))")
    public void controllerMethods() {}
}
