package com.edgareldy.springsecuritytutorial.aspect;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs entry, exit, arguments, and thrown exceptions for every method on
 * every {@code @Service} bean, demonstrating {@code @Around} advice kept
 * entirely outside the business logic it observes. Record components named
 * "password" or "token" (case insensitive) are redacted before logging, so
 * raw credentials and JWTs never end up in application logs.
 * <p>
 * Created by edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.edgareldy.springsecuritytutorial.service..*(..))")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        String arguments = Arrays.stream(joinPoint.getArgs())
                .map(this::describe)
                .collect(Collectors.joining(", "));
        log.info("Entering {} with arguments [{}]", signature, arguments);
        try {
            Object result = joinPoint.proceed();
            log.info("Exiting {} with result {}", signature, describe(result));
            return result;
        } catch (Throwable ex) {
            log.error("Exception in {}: {}", signature, ex.getMessage());
            throw ex;
        }
    }

    private String describe(Object value) {
        if (value == null) {
            return "null";
        }
        Class<?> type = value.getClass();
        if (!type.isRecord()) {
            return value.toString();
        }
        return Arrays.stream(type.getRecordComponents())
                .map(component -> component.getName() + "=" + describeComponent(component, value))
                .collect(Collectors.joining(", ", type.getSimpleName() + "[", "]"));
    }

    private Object describeComponent(RecordComponent component, Object record) {
        String name = component.getName().toLowerCase(Locale.ROOT);
        if (name.contains("password") || name.contains("token")) {
            return "***";
        }
        try {
            return component.getAccessor().invoke(record);
        } catch (ReflectiveOperationException ex) {
            return "?";
        }
    }
}
