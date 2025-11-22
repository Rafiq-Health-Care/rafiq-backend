package com.nexaworks.rafiq.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.GroupIsAlreadyExistsException;
import com.nexaworks.rafiq.exception.custom.GroupNotFoundException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GroupExceptionHandler {
    private final ExceptionUtils exceptionUtils;
    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGroupNotFoundException(
            GroupNotFoundException exception, HttpServletRequest request) {

        ErrorResponse errorResponse = exceptionUtils.getErrorResponse(exception, request,
                HttpStatus.NOT_FOUND);
        return ResponseEntity.status(404).body(errorResponse);
    }
    @ExceptionHandler(GroupIsAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleGroupIsAlreadyExistsException(
            GroupIsAlreadyExistsException exception, HttpServletRequest request) {

        ErrorResponse errorResponse = exceptionUtils.getErrorResponse(exception, request,
                HttpStatus.CONFLICT);
        return ResponseEntity.status(409).body(errorResponse);
    }
}
