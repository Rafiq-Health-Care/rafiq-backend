package com.nexaworks.rafiq.test.doctor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.nexaworks.rafiq.doctor.entity.model.Doctor;
import com.nexaworks.rafiq.doctor.entity.model.Specialization;
import com.nexaworks.rafiq.doctor.repository.DoctorRepository;
import com.nexaworks.rafiq.doctor.service.SpecializationService;
import com.nexaworks.rafiq.doctor.service.implementation.DoctorServiceImpl;
import com.nexaworks.rafiq.shared.entity.FileCategory;
import com.nexaworks.rafiq.shared.event.doctor.DoctorRegisterEvent;
import com.nexaworks.rafiq.shared.event.doctor.UploadFile;
import com.nexaworks.rafiq.shared.event.patient.PatientRegistrationEvent;
import com.nexaworks.rafiq.user.entity.model.User;

@DisplayName("DoctorService Test Cases")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecializationService specializationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        PatientRegistrationEvent basicInfo = new PatientRegistrationEvent(doctor.getEmail(),
                "123456", doctor.getFirstName(), doctor.getLastName(), doctor.getId());
        DoctorRegisterEvent event = new DoctorRegisterEvent(basicInfo, doctor.getId(), null,
                specializationId);

        when(specializationService.getSpecialization(specializationId)).thenReturn(specialization);
        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        doctorService.register(event);

        // then
        verify(specializationService, times(1)).getSpecialization(specializationId);
        verify(doctorRepository, times(1))
                .save(argThat(savedDoctor -> savedDoctor.getId().equals(doctor.getId())
                        && savedDoctor.getEmail().equals(doctor.getEmail())
                        && savedDoctor.getFirstName().equals(doctor.getFirstName())
                        && savedDoctor.getLastName().equals(doctor.getLastName())
                        && savedDoctor.getSpecialization().equals(specialization)));

        ArgumentCaptor<UploadFile> eventCaptor = ArgumentCaptor.forClass(UploadFile.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        UploadFile capturedEvent = eventCaptor.getValue();
        assertEquals(doctor.getId(), capturedEvent.doctorId());
        assertEquals(FileCategory.NATIONAL_ID, capturedEvent.category());
        assertEquals(event.nationalId(), capturedEvent.file());
    }
}
