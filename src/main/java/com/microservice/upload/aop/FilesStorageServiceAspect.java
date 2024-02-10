package com.microservice.upload.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Aspect
@Component
@Slf4j
public class FilesStorageServiceAspect {

    @Around("execution(* com.microservice.upload.service.FilesStorageServiceImpl.init(..))")
    public Object InitAdvice(ProceedingJoinPoint joinPoint) {
        try {
            Object result = joinPoint.proceed();
            log.info("Upload directory created!");
            return result;
        } catch (Throwable throwable) {
            log.error("An error occurred during initialization: {}", throwable.getMessage());
            throw new RuntimeException("An error occurred during initialization", throwable);
        }
    }

    @Around("execution(* com.microservice.upload.service.FilesStorageServiceImpl.uploadFiles(..))")
    public Object uploadFilesAdvice(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("Exception at: " + joinPoint.getSignature().toShortString());
            throw new RuntimeException("Exception at: " + joinPoint.getSignature().toShortString(), throwable);
        }
    }

    @Around("execution(* com.microservice.upload.service.FilesStorageServiceImpl.getFile(..))")
    public Object getFileAdvice(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            return ResponseEntity.status(404).build();
        }
    }

    @Around("execution(* com.microservice.upload.service.FilesStorageServiceImpl.delete(String)) && args(filename)")
    public ResponseEntity<?> handleDeleteException(ProceedingJoinPoint joinPoint, String filename) {
        try {
            return (ResponseEntity<?>) joinPoint.proceed();
        } catch (IOException e) {
            log.error(String.format("delete wrong %s", filename));
            return ResponseEntity.badRequest().body(e);
        } catch (Throwable throwable) {
            String errorMessage = "An unexpected error occurred";
            return ResponseEntity.status(500).body(errorMessage);
        }
    }
}