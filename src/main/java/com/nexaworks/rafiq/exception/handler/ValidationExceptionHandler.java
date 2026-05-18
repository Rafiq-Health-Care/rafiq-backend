package com.nexaworks.rafiq.exception.handler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.model.ValidationErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildResponse("Validation failed", errors, HttpStatus.BAD_REQUEST,
                request.getRequestURI());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBindException(BindException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildResponse("Validation failed", errors, HttpStatus.BAD_REQUEST,
                request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath() != null
                    ? violation.getPropertyPath().toString()
                    : "parameter";
            errors.put(path, violation.getMessage());
        }
        return buildResponse("Validation failed", errors, HttpStatus.BAD_REQUEST,
                request.getRequestURI());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(ValidationException ex,
            HttpServletRequest request) {
        // Generic validation exception without field-level details
        return buildResponse(ex.getMessage() != null ? ex.getMessage() : "Validation failed",
                new HashMap<>(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    private ResponseEntity<ValidationErrorResponse> buildResponse(String message,
            Map<String, String> errors, HttpStatus status, String path) {
        ValidationErrorResponse body = ValidationErrorResponse.builder().status(status.value())
                .error(status.getReasonPhrase()).message(message).code("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now()).path(path).validationErrors(errors).build();
        return new ResponseEntity<>(body, status);
    }
}
