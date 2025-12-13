package com.nexaworks.rafiq.shared.exception;

import java.time.LocalDateTime;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.shared.exception.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionUtils {

    @NotNull
    public ErrorResponse getErrorResponse(Exception ex, HttpServletRequest request,
            HttpStatus status) {

        return new ErrorResponse(status.value(), status.getReasonPhrase(), ex.getMessage(),
                LocalDateTime.now(), request.getRequestURI());
    }
}
