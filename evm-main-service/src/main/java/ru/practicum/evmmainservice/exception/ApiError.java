package ru.practicum.evmmainservice.exception;

import org.springframework.http.HttpStatus;

public class ApiError {

    private final HttpStatus status;
    private final String reason;
    private final String message;
    private final String timestamp;

    public ApiError(HttpStatus status, String reason, String message, String timestamp) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = timestamp;
    }
}
