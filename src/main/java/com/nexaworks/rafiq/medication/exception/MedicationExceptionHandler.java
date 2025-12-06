package com.nexaworks.rafiq.medication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.shared.exception.ExceptionUtils;
import com.nexaworks.rafiq.shared.exception.model.ErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class MedicationExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(MedicineAlreadyExist.class)
    @ApiResponse(responseCode = "409", description = "Medicine already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleMedicineAlreadyExist(MedicineAlreadyExist ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(MedicineLimit.class)
    @ApiResponse(responseCode = "422", description = "Medicine limit exceeded", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleMedicineLimit(MedicineLimit ex,
            HttpServletRequest request) {
        return ResponseEntity.status(422).body(
                exceptionUtils.getErrorResponse(ex, request, HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @ExceptionHandler(MedicineNotFound.class)
    @ApiResponse(responseCode = "404", description = "Medicine not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleMedicineNotFound(MedicineNotFound ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ReminderNotFound.class)
    @ApiResponse(responseCode = "404", description = "Reminder not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleReminderNotFoundException(ReminderNotFound ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(GroupNotFoundException.class)
    @ApiResponse(responseCode = "404", description = "Group not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleGroupNotFoundException(
            GroupNotFoundException exception, HttpServletRequest request) {

        ErrorResponse errorResponse = exceptionUtils.getErrorResponse(exception, request,
                HttpStatus.NOT_FOUND);
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(GroupIsAlreadyExistsException.class)
    @ApiResponse(responseCode = "409", description = "Group already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleGroupIsAlreadyExistsException(
            GroupIsAlreadyExistsException exception, HttpServletRequest request) {

        ErrorResponse errorResponse = exceptionUtils.getErrorResponse(exception, request,
                HttpStatus.CONFLICT);
        return ResponseEntity.status(409).body(errorResponse);
    }
}
