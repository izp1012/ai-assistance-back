package com.uf.assistance.handler.exception;

public class CustomApiException extends RuntimeException {
    public CustomApiException() {
    }

    public CustomApiException(String message) {
        super(message);
    }
}
