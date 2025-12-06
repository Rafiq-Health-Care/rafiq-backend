package com.nexaworks.rafiq.shared.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.shared.exception.ExceptionUtils;
import com.nexaworks.rafiq.shared.exception.custom.SpecializationAlreadyExistsException;
import com.nexaworks.rafiq.shared.exception.custom.SpecializationNotFoundException;
import com.nexaworks.rafiq.shared.exception.custom.SpecializationValidationException;
import com.nexaworks.rafiq.shared.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class SpecializationExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(SpecializationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SpecializationNotFoundException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(SpecializationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(
            SpecializationAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(SpecializationValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(SpecializationValidationException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.BAD_REQUEST));
    }
}
