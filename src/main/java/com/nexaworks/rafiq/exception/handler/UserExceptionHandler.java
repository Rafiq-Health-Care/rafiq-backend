package com.nexaworks.rafiq.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.auth.GoogleAuthException;
import com.nexaworks.rafiq.exception.custom.user.InvalidPasswordException;
import com.nexaworks.rafiq.exception.custom.user.RegistrationException;
import com.nexaworks.rafiq.exception.custom.user.UserException;
import com.nexaworks.rafiq.exception.custom.user.UserNotFoundException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UserExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationException(RegistrationException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(GoogleAuthException.class)
    public ResponseEntity<ErrorResponse> handleGoogleAuthException(GoogleAuthException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.UNAUTHORIZED));
    }
}
