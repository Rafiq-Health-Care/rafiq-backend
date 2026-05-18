package com.nexaworks.rafiq.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexaworks.rafiq.exception.ExceptionUtils;
import com.nexaworks.rafiq.exception.custom.payment.CanNotRefundException;
import com.nexaworks.rafiq.exception.custom.payment.PaymentException;
import com.nexaworks.rafiq.exception.custom.payment.PaymentProviderException;
import com.nexaworks.rafiq.exception.custom.payment.RefundNotFoundException;
import com.nexaworks.rafiq.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class PaymentExceptionHandler {
    private final ExceptionUtils exceptionUtils;

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(PaymentProviderException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProviderException(PaymentProviderException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(CanNotRefundException.class)
    public ResponseEntity<ErrorResponse> handleCanNotRefund(CanNotRefundException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }

    @ExceptionHandler(RefundNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRefundNotFound(RefundNotFoundException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(exceptionUtils.getErrorResponse(ex, request, status));
    }
}
