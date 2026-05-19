package com.nexaworks.rafiq.exception;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.exception.model.ErrorResponse;

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
        String code = resolveCode(ex, status);
        return new ErrorResponse(status.value(), status.getReasonPhrase(), ex.getMessage(), code,
                LocalDateTime.now(), request.getRequestURI());
    }

    private String resolveCode(Exception ex, HttpStatus status) {
        String code = extractCode(ex.getClass());
        return code != null ? code : status.name();
    }

    private String extractCode(Class<?> exceptionType) {
        try {
            Field field = exceptionType.getDeclaredField("CODE");
            if (!Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            field.setAccessible(true);
            Object value = field.get(null);
            return value instanceof String ? (String) value : null;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            return null;
        }
    }
}
