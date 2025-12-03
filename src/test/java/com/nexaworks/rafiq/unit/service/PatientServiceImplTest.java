package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.ServiceImpl.PatientServiceImpl;

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

    @Test
    void shouldCreatePatientProfileSuccessfully() {

        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Patient createdPatient = patientService.createPatientProfile(this.patient);
        assertThat(createdPatient).isNotNull();
        // Patient now extends User, so it should have the same ID and properties
        assertThat(createdPatient.getId()).isEqualTo(this.patient.getId());
        assertThat(createdPatient.getEmail()).isEqualTo(this.patient.getEmail());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

}
