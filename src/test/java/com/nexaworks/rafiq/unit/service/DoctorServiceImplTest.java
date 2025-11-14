package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Status;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.ServiceImpl.DoctorServiceImpl;
import com.nexaworks.rafiq.service.SpecializationService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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

        DoctorProfile savedProfile = new DoctorProfile();
        savedProfile.setId(UUID.randomUUID());
        when(doctorRepository.save(any(DoctorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        DoctorProfile result = doctorService.createProfile(doctor, description, specializationId, nationalId, logo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(doctor);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getSpecialization()).isEqualTo(specialization);
        assertThat(result.getNationalId()).isEqualTo(nationalId);
        assertThat(result.getPublicId()).isEqualTo(logo);
        assertThat(result.getStatus()).isEqualTo(Status.IN_REVIEW);

        verify(specializationService, times(1)).getSpecialization(specializationId);
        verify(doctorRepository, times(1)).save(any(DoctorProfile.class));
    }
}
