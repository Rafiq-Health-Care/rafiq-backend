package com.nexaworks.rafiq.exception.handler;

import com.nexaworks.rafiq.dto.response.ErrorResponse;
import com.nexaworks.rafiq.exception.RegistrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RegistrationExceptionHandler {

    public ResponseEntity<ErrorResponse> handleRegistrationException(RegistrationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Registration Failed",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
