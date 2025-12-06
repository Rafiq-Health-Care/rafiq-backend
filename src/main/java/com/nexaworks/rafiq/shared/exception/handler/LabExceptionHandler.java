package com.nexaworks.rafiq.shared.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.shared.exception.ExceptionUtils;
import com.nexaworks.rafiq.shared.exception.custom.LabException;
import com.nexaworks.rafiq.shared.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class LabExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(LabException.class)
    public ResponseEntity<ErrorResponse> handleLabException(LabException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse error = exceptionUtils.getErrorResponse(ex, request, status);
        return new ResponseEntity<>(error, status);
    }
}
