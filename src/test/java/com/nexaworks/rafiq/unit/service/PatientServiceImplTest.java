package com.nexaworks.rafiq.unit.service;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.patient.implementation.PatientServiceImpl;

@DisplayName("PatientService Test Cases")
public class PatientServiceImplTest {
    @Mock
    PatientRepository patientRepository;

    @InjectMocks
    PatientServiceImpl patientService;

    User patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = new User();
        patient.setId(UUID.randomUUID());
        patient.setEmail("patient@test.com");
        patient.setFirstName("John");
        patient.setLastName("Doe");
    }

}
