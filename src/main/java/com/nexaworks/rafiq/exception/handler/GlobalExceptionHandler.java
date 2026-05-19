package com.nexaworks.rafiq.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.general.MailSenderException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                exceptionUtils.getErrorResponse(ex, request, HttpStatus.UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_ACCEPTABLE));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(404)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(MailSenderException.class)
    public ResponseEntity<ErrorResponse> handleMailSenderException(MailSenderException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }
}
