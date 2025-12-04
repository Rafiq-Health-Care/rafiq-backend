package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.Status;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.ServiceImpl.DoctorServiceImpl;
import com.nexaworks.rafiq.service.SpecializationService;

@DisplayName("DoctorService Test Cases")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecializationService specializationService;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private User doctor;
    private Specialization specialization;
    private UUID specializationId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doctor = new User();
        doctor.setId(UUID.randomUUID());
        doctor.setEmail("doctor@test.com");
        doctor.setFirstName("Jane");
        doctor.setLastName("Smith");

        specializationId = UUID.randomUUID();
        specialization = new Specialization();
        specialization.setId(specializationId);
    }

    @DisplayName("Should create doctor profile successfully")
    @Test
    void shouldCreateDoctorProfileSuccessfully() {
        // given
        String description = "Experienced Cardiologist";
        String nationalId = "123456789";
        String logo = "logo_public_id";

        when(specializationService.getSpecialization(specializationId)).thenReturn(specialization);

        Doctor savedProfile = new Doctor();
        savedProfile.setId(UUID.randomUUID());
        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Doctor result = doctorService.createProfile(doctor, description, specializationId);

        // then
        assertThat(result).isNotNull();
        // Doctor now extends User, so it should have the same ID and properties
        assertThat(result.getId()).isEqualTo(doctor.getId());
        assertThat(result.getEmail()).isEqualTo(doctor.getEmail());
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getSpecialization()).isEqualTo(specialization);
        assertThat(result.getStatus()).isEqualTo(Status.IN_REVIEW);

        verify(specializationService, times(1)).getSpecialization(specializationId);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }
}
