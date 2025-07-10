package ua.azaika.serverpulse.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ua.azaika.serverpulse.dto.auth.SignInRequestDTO;
import ua.azaika.serverpulse.dto.auth.SignUpRequestDTO;
import ua.azaika.serverpulse.exception.UserAlreadyExistsException;

/**
 * @author Andrii Zaika
 */
@Aspect
@Component
@Slf4j
public class AuthenticationLoggingAspect {

    @Pointcut("execution(* ua.azaika.serverpulse.service.AuthenticationService.signUp(..))")
    public void authenticationSignUp() {}

    @Pointcut("execution(* ua.azaika.serverpulse.service.AuthenticationService.signIn(..))")
    public void authenticationSignIn() {}

    @Around("authenticationSignUp()")
    public Object logAuthenticationSignUp(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String loginToLog = "UNKNOWN_LOGIN";

        if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof SignUpRequestDTO signUpRequestDTO) {
            loginToLog = signUpRequestDTO.username();
        }

        Object result;

        try {
            log.debug("[{}][{}] Attempting sign up for user: {}", className, methodName, loginToLog);
            result = joinPoint.proceed();
            log.debug("[{}][{}] User: {} signed up successfully", className, methodName, loginToLog);
        } catch (UserAlreadyExistsException e) {
            log.warn("[{}][{}] Sign up failed for user {}: {}", className, methodName, loginToLog, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[{}][{}] Unexpected error during sign up for user: {}", className, methodName, loginToLog, e);
            throw e;
        }
        return result;
    }

    @Around("authenticationSignIn()")
    public Object logAuthenticationSignIn(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String loginToLog = "UNKNOWN_LOGIN";

        if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof SignInRequestDTO signInRequestDTO) {
            loginToLog = signInRequestDTO.login();
        }

        Object result;

        try {
            log.debug("[{}][{}] Attempting sign in for user: {}", className, methodName, loginToLog);
            result = joinPoint.proceed();
            log.debug("[{}][{}] User: {} signed in successfully", className, methodName, loginToLog);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.warn("[{}][{}] Sign in failed for user {}: {}", className, methodName, loginToLog, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[{}][{}] Unexpected error during sign in for user: {}", className, methodName, loginToLog, e);
            throw e;
        }
        return result;
    }
}