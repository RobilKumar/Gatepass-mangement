package com.company.gatepass.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    // Stores an HTTP status together with the user-facing API error message.
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    // Exposes the HTTP status so the global exception handler can build the response.
    public HttpStatus getStatus() {
        return status;
    }
}
