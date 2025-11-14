package com.nexaworks.rafiq.exception.handler;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.SpecializationAlreadyExistsException;
import com.nexaworks.rafiq.exception.custom.SpecializationNotFoundException;
import com.nexaworks.rafiq.exception.custom.SpecializationValidationException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class SpecializationExceptionHandler {
  private final ExceptionUtils exceptionUtils;

  @ExceptionHandler(SpecializationNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      SpecializationNotFoundException ex, HttpServletRequest request) {
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
  public ResponseEntity<ErrorResponse> handleValidation(
      SpecializationValidationException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.BAD_REQUEST));
  }
}
