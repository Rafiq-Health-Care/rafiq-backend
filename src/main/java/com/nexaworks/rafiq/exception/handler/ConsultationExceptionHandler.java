package com.nexaworks.rafiq.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.consultation.CanNotCancelConsultation;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationOverlappingException;
import com.nexaworks.rafiq.exception.custom.consultation.RtcProviderException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotCanNotCreated;
import com.nexaworks.rafiq.exception.custom.consultation.SlotCanNotEditException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotReservedException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ConsultationExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(SlotCanNotCreated.class)
    public ResponseEntity<ErrorResponse> handleSlotCanNotCreated(SlotCanNotCreated ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(SlotCanNotEditException.class)
    public ResponseEntity<ErrorResponse> handleSlotCanNotEdit(SlotCanNotEditException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(SlotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSlotNotFound(SlotNotFoundException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(SlotReservedException.class)
    public ResponseEntity<ErrorResponse> handleSlotReserved(SlotReservedException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(ConsultationOverlappingException.class)
    public ResponseEntity<ErrorResponse> handleConsultationOverlapping(
            ConsultationOverlappingException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(ConsultationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConsultationNotFound(
            ConsultationNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(CanNotCancelConsultation.class)
    public ResponseEntity<ErrorResponse> handleCanNotCancelConsultation(CanNotCancelConsultation ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(RtcProviderException.class)
    public ResponseEntity<ErrorResponse> handleRtcProviderException(RtcProviderException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }
}
