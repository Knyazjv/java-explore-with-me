package ru.practicum.evmmainservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private final String REASON_NOT_FOUND = "The required object was not found.";
    private final String REASON_BAD_REQUEST = "Incorrectly made request.";
    private final String REASON_FORBIDDEN = "For the requested operation the conditions are not met.";
    private final String CONSTRAINT_VIOLATED = "Integrity constraint has been violated.";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("NOT_FOUND: " + e.getMessage());
        return new ApiError(HttpStatus.NOT_FOUND,
                REASON_NOT_FOUND, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException e) {
        log.warn("BAD_REQUEST: " + e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST,
                REASON_BAD_REQUEST, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(final ForbiddenException e) {
        log.warn("BAD_REQUEST: " + e.getMessage());
        return new ApiError(HttpStatus.FORBIDDEN,
                REASON_FORBIDDEN, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleCategoryException(final ConflictException e) {
        log.warn("CONFLICT: " + e.getMessage());
        return new ApiError(HttpStatus.CONFLICT,
                REASON_FORBIDDEN, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleCategoryException(final Exception e) {
        log.warn("CONFLICT: " + e.getMessage());
        return new ApiError(HttpStatus.CONFLICT,
                REASON_FORBIDDEN, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintException(final ConstraintException e) {
        log.warn("CONFLICT: " + e.getMessage());
        return new ApiError(HttpStatus.CONFLICT,
                CONSTRAINT_VIOLATED, e.getMessage(), LocalDateTime.now().format(formatter));
    }
}