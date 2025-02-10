package com.uf.assistance.handler.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.util.CustomRequestUtil;
import com.uf.assistance.util.RequestIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class LoggingAspect {

    //TODO Request 와 Response 로그 저장하기
    //TODO id, ip, userid, method, request uri, request body
    //TODO id, ip, userid, method, response body, http status
    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    private final ObjectMapper objectMapper;

    @Before("execution(* com.uf.assistance.web..*(..))")
    public void beforeRequest(final JoinPoint joinPoint) {

        String requestId = UUID.randomUUID().toString();
        RequestIdHolder.set(requestId);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("[{}] REQUEST: {} {} {} {}", requestId, request.getMethod(), request.getRequestURI(),
                    CustomRequestUtil.getClientIp(request), CustomRequestUtil.getUserId(request));
        }

        // 요청 바디 로깅
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null && arg.getClass().isAnnotationPresent(RequestBody.class)) {
                try {
                    log.info("[{}] REQUEST BODY: {}", requestId, objectMapper.writeValueAsString(arg));
                } catch (Exception e) {
                    log.error("Failed to log request body", e);
                }
            }
        }
    }

    @AfterReturning(pointcut = "execution(* com.uf.assistance.web..*(..))", returning = "response")
    public void afterResponse(JoinPoint joinPoint, Object response) {
        String requestId = RequestIdHolder.get();
        if (response != null) {
            try {
                log.info("[{}] RESPONSE: {}", requestId, objectMapper.writeValueAsString(response));
            } catch (Exception e) {
                log.error("Failed to log response", e);
            }
        }

        // 요청이 끝나면 ThreadLocal 초기화 (메모리 누수 방지 및 개인정보 유출 방지)
        RequestIdHolder.clear();
    }
}
