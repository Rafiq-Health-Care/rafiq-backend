package com.nexaworks.rafiq.exception.handler;


import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;


import java.time.LocalDateTime;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ExceptionUtils exceptionUtils;
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(405).body(new ErrorResponse(405, "Method Not Allowed",ex.getMessage(), LocalDateTime.now(),request.getRequestURI()));
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(404).body(
            exceptionUtils.getErrorResponse(ex,request, HttpStatus.NOT_FOUND)

        );
    }

}
