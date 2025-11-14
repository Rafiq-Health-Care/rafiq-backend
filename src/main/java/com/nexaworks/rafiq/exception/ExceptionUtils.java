package com.nexaworks.rafiq.exception;

import com.nexaworks.rafiq.exception.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionUtils {

  @NotNull
  public ErrorResponse getErrorResponse(
      Exception ex, HttpServletRequest request, HttpStatus status) {

    return new ErrorResponse(
        status.value(),
        status.getReasonPhrase(),
        ex.getMessage(),
        LocalDateTime.now(),
        request.getRequestURI());
  }
}
