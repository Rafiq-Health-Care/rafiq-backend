package com.nexaworks.rafiq.user.validation.annotation;

import java.lang.annotation.*;

import com.nexaworks.rafiq.user.validation.validator.AgeValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAge {
    String message() default "Invalid age. Age must be between 0 and 120.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
