package ru.practicum.evmmainservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@AllArgsConstructor
public class ApiError {

    private final HttpStatus status;
    private final String reason;
    private final String message;
    private final String timestamp;
    private final String stackTrace;
}
