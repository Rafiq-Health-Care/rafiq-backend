package com.nexaworks.rafiq.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.shared.exception.ExceptionUtils;
import com.nexaworks.rafiq.shared.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
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
    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalid(TokenInvalidException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse error = exceptionUtils.getErrorResponse(ex, request, status);
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotfoundException(TokenNotFoundException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse error = exceptionUtils.getErrorResponse(ex, request, status);
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse error = exceptionUtils.getErrorResponse(ex, request, status);
        return new ResponseEntity<>(error, status);
    }
}
