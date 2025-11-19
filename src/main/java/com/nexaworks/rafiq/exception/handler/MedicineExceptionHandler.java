package com.nexaworks.rafiq.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.MedicineAlreadyExist;
import com.nexaworks.rafiq.exception.custom.MedicineLimit;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
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
}
