package ru.practicum.evmmainservice.exception.exception;

public class ConstraintException extends RuntimeException {
    public ConstraintException(String message) {
        super(message);
    }
}