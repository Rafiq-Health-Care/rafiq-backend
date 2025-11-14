package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.ServiceImpl.PatientServiceImpl;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("PatientService Test Cases")
public class PatientServiceImplTest {
  @Mock PatientRepository patientRepository;

  @InjectMocks PatientServiceImpl patientService;

  User patient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    patient = new User();
    patient.setId(UUID.randomUUID());
  }

  @Test
  void shouldCreatePatientProfileSuccessfully() {

    when(patientRepository.save(any(PatientProfile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    PatientProfile patientProfile = patientService.createPatientProfile(patient);
    assertThat(patientProfile).isNotNull();
    assertThat(patientProfile.getUser()).isEqualTo(patient);
    verify(patientRepository, times(1)).save(any(PatientProfile.class));
  }
}
