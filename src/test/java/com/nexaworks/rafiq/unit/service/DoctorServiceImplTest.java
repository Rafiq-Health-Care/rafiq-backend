package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Education;
import com.nexaworks.rafiq.entities.Experience;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.mapper.DoctorMapper;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.doctor.DoctorServiceImpl;

@DisplayName("DoctorService Test Cases")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AuthService authService;

    @Mock
    private DoctorMapper doctorMapper;

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

        when(doctorMapper.toEducationEntity(any(EducationItemRequest.class)))
                .thenAnswer(invocation -> {
                    EducationItemRequest r = invocation.getArgument(0);
                    Education e = new Education();
                    e.setDegree(r.degree());
                    e.setUniversity(r.university());
                    e.setStartYear(r.startYear());
                    e.setEndYear(r.endYear());
                    return e;
                });
        when(doctorMapper.toExperienceEntity(any(ExperienceItemRequest.class)))
                .thenAnswer(invocation -> {
                    ExperienceItemRequest r = invocation.getArgument(0);
                    Experience e = new Experience();
                    e.setPosition(r.position());
                    e.setHospital(r.hospital());
                    e.setStartYear(r.startYear());
                    e.setEndYear(r.endYear());
                    e.setDescription(r.description());
                    return e;
                });
    }

    @DisplayName("Should register doctor successfully")
    @Test
    void shouldRegisterDoctorSuccessfully() {
        String description = "Experienced Cardiologist";
        Doctor doctorEntity = new Doctor();
        doctorEntity.setId(doctor.getId());
        doctorEntity.setEmail(doctor.getEmail());
        doctorEntity.setFirstName(doctor.getFirstName());
        doctorEntity.setLastName(doctor.getLastName());

        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doctorService.register(doctorEntity, specializationEnum, description);

        verify(doctorRepository, times(1))
                .save(argThat(savedDoctor -> savedDoctor.getDescription().equals(description)
                        && savedDoctor.getSpecialization().equals(specializationEnum)));
    }

    @Test
    @DisplayName("Should replace education for authenticated doctor")
    void shouldReplaceEducation() {
        Doctor doctorEntity = baseDoctorEntity();
        when(authService.getAuthenticateUserId()).thenReturn(doctor.getId());
        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctorEntity));
        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doctorService
                .replaceEducation(List.of(new EducationItemRequest("MD", "Harvard", 2011, 2015)));

        assertThat(doctorEntity.getEducation()).hasSize(1);
        assertThat(doctorEntity.getEducation().get(0).getStartYear()).isEqualTo(2011);
        assertThat(doctorEntity.getEducation().get(0).getEndYear()).isEqualTo(2015);
    }

    @Test
    @DisplayName("Should accept experience with null end year (present role)")
    void shouldAcceptPresentExperience() {
        Doctor doctorEntity = baseDoctorEntity();
        when(authService.getAuthenticateUserId()).thenReturn(doctor.getId());
        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctorEntity));
        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int started = java.time.Year.now().getValue() - 2;
        doctorService.replaceExperience(List
                .of(new ExperienceItemRequest("Consultant", "General", started, null, "Details")));

        assertThat(doctorEntity.getExperience()).hasSize(1);
        assertThat(doctorEntity.getExperience().get(0).getEndYear()).isNull();
    }

    private Doctor baseDoctorEntity() {
        Doctor doctorEntity = new Doctor();
        doctorEntity.setId(doctor.getId());
        doctorEntity.setEmail(doctor.getEmail());
        doctorEntity.setFirstName(doctor.getFirstName());
        doctorEntity.setLastName(doctor.getLastName());
        return doctorEntity;
    }
}
