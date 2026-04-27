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
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.doctor.DoctorServiceImpl;

@DisplayName("DoctorService Test Cases")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private User doctor;
    private com.nexaworks.rafiq.entities.enums.Specialization specializationEnum;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doctor = new User();
        doctor.setId(UUID.randomUUID());
        doctor.setEmail("doctor@test.com");
        doctor.setFirstName("Jane");
        doctor.setLastName("Smith");

        specializationEnum = com.nexaworks.rafiq.entities.enums.Specialization.ALLERGY;

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


        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        doctorService.register(doctorEntity, specializationEnum, description);

        // then

        verify(doctorRepository, times(1))
                .save(argThat(savedDoctor -> savedDoctor.getDescription().equals(description)
                        && savedDoctor.getSpecialization().equals(specializationEnum)));
    }
}
