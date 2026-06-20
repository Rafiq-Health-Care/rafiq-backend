package com.nexaworks.rafiq.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.medicine.MedicineAlreadyExist;
import com.nexaworks.rafiq.exception.custom.medicine.MedicineLimit;
import com.nexaworks.rafiq.exception.custom.medicine.MedicineNotFound;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class MedicineExceptionHandler {
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
}
