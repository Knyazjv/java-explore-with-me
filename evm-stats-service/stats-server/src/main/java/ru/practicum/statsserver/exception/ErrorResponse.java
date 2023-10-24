package ru.practicum.statsserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
    private final HttpStatus status;
    private final String error;
    private final String description;
    private final String stackTrace;

    public ErrorResponse(HttpStatus status, String error, String description, String stackTrace) {
        this.status = status;
        this.error = error;
        this.description = description;
        this.stackTrace = stackTrace;
    }
}