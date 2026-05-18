package com.nexaworks.rafiq.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.auth.AuthorizationException;
import com.nexaworks.rafiq.exception.custom.consultation.CanNotCancelConsultation;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationOverlappingException;
import com.nexaworks.rafiq.exception.custom.consultation.RtcProviderException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotCanNotCreated;
import com.nexaworks.rafiq.exception.custom.consultation.SlotCanNotEditException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotReservedException;
import com.nexaworks.rafiq.exception.custom.general.MailSenderException;
import com.nexaworks.rafiq.exception.custom.payment.CanNotRefundException;
import com.nexaworks.rafiq.exception.custom.payment.PaymentException;
import com.nexaworks.rafiq.exception.custom.payment.PaymentProviderException;
import com.nexaworks.rafiq.exception.custom.payment.RefundNotFoundException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(404)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.NOT_FOUND));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(400)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(exceptionUtils.getErrorResponse(ex, request, HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler({PaymentException.class, PaymentProviderException.class})
    public ResponseEntity<ErrorResponse> handlePaymentException(RuntimeException ex,
            HttpServletRequest request) {
        HttpStatus status = ex instanceof PaymentProviderException
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler({CanNotRefundException.class, RefundNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleRefundException(RuntimeException ex,
            HttpServletRequest request) {
        HttpStatus status = ex instanceof RefundNotFoundException
                ? HttpStatus.NOT_FOUND
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler({SlotCanNotCreated.class, SlotCanNotEditException.class,
            SlotNotFoundException.class, SlotReservedException.class,
            ConsultationOverlappingException.class, ConsultationNotFoundException.class,
            CanNotCancelConsultation.class, RtcProviderException.class})
    public ResponseEntity<ErrorResponse> handleConsultationException(RuntimeException ex,
            HttpServletRequest request) {
        HttpStatus status;
        if (ex instanceof SlotNotFoundException || ex instanceof ConsultationNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof RtcProviderException) {
            status = HttpStatus.BAD_GATEWAY;
        } else if (ex instanceof SlotCanNotCreated) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(MailSenderException.class)
    public ResponseEntity<ErrorResponse> handleMailSenderException(MailSenderException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }
}
