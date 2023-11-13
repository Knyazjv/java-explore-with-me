package ru.practicum.ewmmainservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewmmainservice.exception.exception.*;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final String REASON_NOT_FOUND = "The required object was not found.";
    private static final String REASON_BAD_REQUEST = "Incorrectly made request.";
    private static final String REASON_FORBIDDEN = "For the requested operation the conditions are not met.";
    private static final String CONSTRAINT_VIOLATED = "Integrity constraint has been violated.";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            IllegalStateException.class,
            BadRequestException.class}
    )
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) {
        log.warn("BAD_REQUEST: " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.warn(sw.toString());
        return new ApiError(HttpStatus.BAD_REQUEST,
                REASON_BAD_REQUEST, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler({
            ForbiddenException.class}
    )
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiError handleForbiddenException(final ForbiddenException e) {
        log.warn("CONFLICT: " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.warn(sw.toString());
        return new ApiError(HttpStatus.CONFLICT,
                REASON_FORBIDDEN, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler({
            ConflictException.class,
            DataIntegrityViolationException.class,
            ConstraintException.class}
    )
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiError handleConflictException(final Exception e) {
        log.warn("ERROR: " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.warn(sw.toString());
        return new ApiError(HttpStatus.CONFLICT,
                CONSTRAINT_VIOLATED, e.getMessage(), LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler({
            NotFoundException.class}
    )
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("NOT_FOUND: " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.warn(sw.toString());
        return new ApiError(HttpStatus.NOT_FOUND,
                REASON_NOT_FOUND, e.getMessage(), LocalDateTime.now().format(formatter));
    }
}