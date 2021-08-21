package com.itorix.apiwiz.sso.logging;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Aspect
@Configuration
public class LoggerAspect {
    private static Logger logger = LoggerFactory.getLogger(LoggerAspect.class);
    @Autowired
    LoggerService loggerService;

    @Autowired
    HttpServletResponse httpServletResponse;

    // @Before("(within(com.itorix.hyggee..*.service..*) || within(com.itorix.hyggee..*.serviceImpl..*)) &&
    // execution(public * *(..))")
    @Before("execution(* com.itorix.apiwiz..*.service..*(..)) || execution(* com.itorix.apiwiz..*.serviceImpl..*(..))")
    public void logControllerInput(JoinPoint joinPoint) throws IOException {
        loggerService.logServiceRequest();

    }

    @AfterReturning(pointcut = "execution(public * com.itorix.apiwiz..*.service.*.*(..)) || execution(public * com.itorix.apiwiz..*.serviceImpl.*.*(..))", returning = "result")
    public void loggingMethodResponse(JoinPoint joinPoint, Object result) throws IOException {

        ResponseEntity responseEntity = null;
        if (result instanceof ResponseEntity) {
            responseEntity = (ResponseEntity) result;
            loggerService.logServiceResponse(responseEntity.getStatusCode());
        }
        loggerService.logServiceResponse(HttpStatus.ACCEPTED);
    }

}