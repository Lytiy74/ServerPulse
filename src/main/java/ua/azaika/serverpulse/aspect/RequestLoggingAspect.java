package ua.azaika.serverpulse.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Andrii Zaika
 */
@Aspect
@Component
@Slf4j
public class RequestLoggingAspect {

    @Pointcut("execution(* ua.azaika.serverpulse.controller..*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logControllerRequests(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        Object result = null;

        try {
            result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;

            if (result instanceof ResponseEntity<?> responseEntity) {
                if (responseEntity.getStatusCode().is4xxClientError()) {
                    log.warn("[REQUEST][{}.{}][{} {}] Request processed in {}ms. Status: {}. (Client Error)",
                            className, methodName, method, uri, duration, responseEntity.getStatusCode());
                } else {
                    log.info("[REQUEST][{}.{}][{} {}] Request processed in {}ms. Status: {}.",
                            className, methodName, method, uri, duration, responseEntity.getStatusCode());
                }
            } else {
                log.info("[REQUEST][{}.{}][{} {}] Request processed in {}ms.",
                        className, methodName, method, uri, duration);
            }
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("[REQUEST][{}.{}][{} {}] Request failed in {}ms. Unexpected Error: {}",
                    className, methodName, method, uri, duration, e.getMessage(), e);
            throw e;
        }
    }
}