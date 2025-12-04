package com.nexaworks.rafiq.validation.validator;

import java.time.LocalDate;
import java.time.Period;

import com.nexaworks.rafiq.validation.annotation.ValidAge;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null)
            return false;
        if (value.isAfter(LocalDate.now()))
            return false;
        int age = Period.between(value, LocalDate.now()).getYears();
        return age >= 0 && age <= 120;
    }
}
