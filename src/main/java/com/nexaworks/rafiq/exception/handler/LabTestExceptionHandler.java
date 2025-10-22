package com.nexaworks.rafiq.exception.handler;

import com.nexaworks.rafiq.exception.custom.LabTesException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class LabTestExceptionHandler {

    @ExceptionHandler(LabTesException.class)
    public ResponseEntity<ErrorResponse> handleLabTestException(LabTesException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
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
