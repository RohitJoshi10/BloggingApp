package com.BloggingApp.BloggingApp.exceptions;

public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException() {
        super();
    }
}
