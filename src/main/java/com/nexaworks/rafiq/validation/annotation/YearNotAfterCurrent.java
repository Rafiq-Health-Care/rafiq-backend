package com.nexaworks.rafiq.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nexaworks.rafiq.validation.validator.YearNotAfterCurrentValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = YearNotAfterCurrentValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface YearNotAfterCurrent {
    String message() default "Year cannot be after the current calendar year.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
