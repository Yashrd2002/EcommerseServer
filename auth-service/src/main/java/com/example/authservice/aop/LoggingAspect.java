package com.example.authservice.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.authservice.controller..*(..)) || execution(* com.example.authservice.service..*(..)) || execution(* com.example.authservice.repository..*(..))")
    public void applicationPackagePointcut() {}

    @Before("applicationPackagePointcut()")
    public void logBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        logger.info("Entering method: {}.{} with arguments: {}", signature.getDeclaringTypeName(), signature.getName(), Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(value = "applicationPackagePointcut()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        logger.info("Exiting method: {}.{} with return: {}", signature.getDeclaringTypeName(), signature.getName(), result);
    }

    @AfterThrowing(value = "applicationPackagePointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        logger.error("Exception in method: {}.{}", signature.getDeclaringTypeName(), signature.getName(), exception);
    }
}
