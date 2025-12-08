package com.nexaworks.rafiq.medication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.shared.exception.ExceptionUtils;
import com.nexaworks.rafiq.shared.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class MedicationExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(MedicineAlreadyExist.class)
    public ResponseEntity<ErrorResponse> handleMedicineAlreadyExist(MedicineAlreadyExist ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(MedicineLimit.class)
    public ResponseEntity<ErrorResponse> handleMedicineLimit(MedicineLimit ex,
            HttpServletRequest request) {
        return ResponseEntity.status(422).body(
                exceptionUtils.getErrorResponse(ex, request, HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @ExceptionHandler(MedicineNotFound.class)
    public ResponseEntity<ErrorResponse> handleMedicineNotFound(MedicineNotFound ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ReminderNotFound.class)
    public ResponseEntity<ErrorResponse> handleReminderNotFoundException(ReminderNotFound ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }

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
