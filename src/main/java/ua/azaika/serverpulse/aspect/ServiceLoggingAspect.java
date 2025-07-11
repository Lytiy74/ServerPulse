package ua.azaika.serverpulse.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 @author Andrii Zaika
 **/
@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Pointcut("execution(* ua.azaika.serverpulse.service..*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object loggServiceMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();
        Object[] args = proceedingJoinPoint.getArgs();
        long startTime = System.currentTimeMillis();

        log.info("[{}][{}] Service method invoked with args {}", className, methodName, Arrays.toString(args));

        Object result = null;
        try {
            result = proceedingJoinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}][{}] Service method executed in {}ms. Result type: {}",
                    className, methodName, duration, (result != null ? result.getClass().getSimpleName() : "void"));
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}][{}] Service method failed in {}ms with exception: {}",
                    className, methodName, duration, e.getMessage(), e);
            throw e;
        }
        return result;
    }

}
