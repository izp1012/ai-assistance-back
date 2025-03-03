package com.uf.assistance.handler;

import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.handler.exception.CustomApiException;
import com.uf.assistance.util.CustomDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class CustomExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<?> apiException(CustomApiException e){
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null)
                , HttpStatus.BAD_REQUEST
        );
    }
}
