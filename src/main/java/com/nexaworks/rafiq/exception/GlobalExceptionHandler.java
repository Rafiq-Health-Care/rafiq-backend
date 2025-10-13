package com.nexaworks.rafiq.exception;

import com.nexaworks.rafiq.dto.response.ErrorResponse;
import com.nexaworks.rafiq.dto.response.ValidationErrorResponse;
import com.nexaworks.rafiq.exception.handler.*;
import com.nexaworks.rafiq.exception.SpecializationNotFoundException;
import com.nexaworks.rafiq.exception.SpecializationAlreadyExistsException;
import com.nexaworks.rafiq.exception.SpecializationValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global Exception Handler that coordinates all exception handling.
 * Individual handlers are separated into specific handler classes in the handler package.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final RegistrationExceptionHandler registrationHandler;
    private final UserNotFoundExceptionHandler userNotFoundHandler;
    private final ValidationExceptionHandler validationHandler;
    private final IllegalArgumentExceptionHandler illegalArgumentHandler;
    private final GenericExceptionHandler genericHandler;
    private final AuthenticationEntryPointExceptionHandler authenticationHandler;
    private final AccessDeniedExceptionHandler accessDeniedHandler;
    private final SpecializationExceptionHandler specializationHandler;

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationException(RegistrationException ex, WebRequest request) {
        return registrationHandler.handleRegistrationException(ex, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        return userNotFoundHandler.handleUserNotFoundException(ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return validationHandler.handleValidationExceptions(ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return illegalArgumentHandler.handleIllegalArgumentException(ex, request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return genericHandler.handleRuntimeException(ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        return genericHandler.handleGenericException(ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return authenticationHandler.handleAuthenticationException(ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return accessDeniedHandler.handleAccessDeniedException(ex, request);
    }

    @ExceptionHandler(SpecializationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSpecializationNotFoundException(SpecializationNotFoundException ex, WebRequest request) {
        return specializationHandler.handleSpecializationNotFoundException(ex, request);
    }

    @ExceptionHandler(SpecializationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSpecializationAlreadyExistsException(SpecializationAlreadyExistsException ex, WebRequest request) {
        return specializationHandler.handleSpecializationAlreadyExistsException(ex, request);
    }

    @ExceptionHandler(SpecializationValidationException.class)
    public ResponseEntity<ErrorResponse> handleSpecializationValidationException(SpecializationValidationException ex, WebRequest request) {
        return specializationHandler.handleSpecializationValidationException(ex, request);
    }
}
