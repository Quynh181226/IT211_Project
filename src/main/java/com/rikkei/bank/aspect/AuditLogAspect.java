package com.rikkei.bank.aspect;

import com.rikkei.bank.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    // Ghi log cho tất cả các method trong service layer
    @Before("execution(* com.rikkei.bank.service.*.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        log.info("[START] {}.{} - Arguments: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "execution(* com.rikkei.bank.service.*.*(..))", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, Object result) {
        log.info("[END] {}.{} - Result: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                result != null ? result.toString() : "null");
    }

    @AfterThrowing(pointcut = "execution(* com.rikkei.bank.service.*.*(..))", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        log.error("[ERROR] {}.{} - Exception: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                error.getMessage());
    }

    // Ghi log đặc biệt cho giao dịch chuyển tiền (AUDIT)
    @AfterReturning(pointcut = "execution(* com.rikkei.bank.service.TransferService.transfer(..))", returning = "result")
    public void auditTransfer(JoinPoint joinPoint, Object result) {
        log.info("[AUDIT] TRANSFER completed - Details: {}", result);
    }

    // Đo thời gian thực hiện method có annotation @LogExecutionTime
    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;
        log.info("[PERFORMANCE] {}.{} executed in {} ms",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                executionTime);

        return proceed;
    }
}