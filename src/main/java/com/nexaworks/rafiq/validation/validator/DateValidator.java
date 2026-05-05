package com.nexaworks.rafiq.validation.validator;

import com.nexaworks.rafiq.validation.annotation.FutureDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<FutureDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true; // let @NotNull handle nulls
        return value.isAfter(LocalDate.now());
    }
}