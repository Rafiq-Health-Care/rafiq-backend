package com.nexaworks.rafiq.validation.validator;

import com.nexaworks.rafiq.validation.annotation.FutureDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateValidator implements ConstraintValidator<FutureDate, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.isAfter(LocalDateTime.now().plusMinutes(120));
    }
}