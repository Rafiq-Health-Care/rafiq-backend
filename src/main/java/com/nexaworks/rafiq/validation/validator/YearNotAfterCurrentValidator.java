package com.nexaworks.rafiq.validation.validator;

import java.time.Year;

import com.nexaworks.rafiq.validation.annotation.YearNotAfterCurrent;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class YearNotAfterCurrentValidator
        implements
            ConstraintValidator<YearNotAfterCurrent, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value <= Year.now().getValue();
    }
}
