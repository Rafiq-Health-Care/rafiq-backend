package com.nexaworks.rafiq.unit.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("Doctor profile request bean validation")
class DoctorItemRequestValidatorsTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void educationRejectsStartYearAfterCurrent() {
        int future = Year.now().getValue() + 1;
        var req = new EducationItemRequest("MD", "School", future, null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void educationRejectsEndBeforeStart() {
        int y = Year.now().getValue();
        var req = new EducationItemRequest("MD", "School", y, y - 1);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void educationAcceptsValidRange() {
        int y = Year.now().getValue();
        var req = new EducationItemRequest("MD", "School", y - 10, y - 4);
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void experienceRejectsStartYearAfterCurrent() {
        int future = Year.now().getValue() + 2;
        var req = new ExperienceItemRequest("Role", "Hosp", future, null, null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void experienceRejectsCompletedEndAfterCurrent() {
        int y = Year.now().getValue();
        var req = new ExperienceItemRequest("Role", "Hosp", y - 5, y + 1, null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void experienceRejectsEndBeforeStart() {
        int y = Year.now().getValue();
        var req = new ExperienceItemRequest("Role", "Hosp", y, y - 1, null);
        Set<ConstraintViolation<ExperienceItemRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void experienceAcceptsOpenEnded() {
        int y = Year.now().getValue() - 1;
        var req = new ExperienceItemRequest("Role", "Hosp", y, null, null);
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void experienceAcceptsCompletedWithinPast() {
        int y = Year.now().getValue();
        var req = new ExperienceItemRequest("Role", "Hosp", y - 10, y - 2, null);
        assertThat(validator.validate(req)).isEmpty();
    }
}
