package com.nexaworks.rafiq.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.labtest.LabTestException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LabTestExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(LabTestException.class)
    public ResponseEntity<ErrorResponse> handleLabTestException(LabTestException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse error = exceptionUtils.getErrorResponse(ex, request, status);
        return new ResponseEntity<>(error, status);
    }
}
