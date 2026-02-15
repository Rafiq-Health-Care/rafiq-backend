package com.nexaworks.rafiq.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.doctor.DoctorServiceImpl;
import com.nexaworks.rafiq.service.doctor.SpecializationService;

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

    @DisplayName("Should register doctor successfully")
    @Test
    void shouldRegisterDoctorSuccessfully() {
        // given
        String description = "Experienced Cardiologist";
        Doctor doctorEntity = new Doctor();
        doctorEntity.setId(doctor.getId());
        doctorEntity.setEmail(doctor.getEmail());
        doctorEntity.setFirstName(doctor.getFirstName());
        doctorEntity.setLastName(doctor.getLastName());

        when(specializationService.getSpecialization(specializationId)).thenReturn(specialization);
        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        doctorService.register(doctorEntity, specializationId, description);

        // then
        verify(specializationService, times(1)).getSpecialization(specializationId);
        verify(doctorRepository, times(1))
                .save(argThat(savedDoctor -> savedDoctor.getDescription().equals(description)
                        && savedDoctor.getSpecialization().equals(specialization)));
    }
}
