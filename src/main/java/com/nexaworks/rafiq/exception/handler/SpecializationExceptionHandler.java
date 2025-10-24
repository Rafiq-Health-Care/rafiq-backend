package com.nexaworks.rafiq.exception.handler;

import com.nexaworks.rafiq.exception.custom.SpecializationAlreadyExistsException;
import com.nexaworks.rafiq.exception.custom.SpecializationNotFoundException;
import com.nexaworks.rafiq.exception.custom.SpecializationValidationException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class SpecializationExceptionHandler {

    @ExceptionHandler(SpecializationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SpecializationNotFoundException ex, HttpServletRequest request) {
        return build(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SpecializationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(SpecializationAlreadyExistsException ex, HttpServletRequest request) {
        return build(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SpecializationValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(SpecializationValidationException ex, HttpServletRequest request) {
        return build(ex, request, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponse> build(RuntimeException ex, HttpServletRequest request, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }
}
