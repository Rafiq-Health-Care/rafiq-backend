package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.SetDoctorPriceRequest;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.UserRepository;

@DisplayName("Doctor Controller Integration Tests")
public class DoctorControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ME_EDUCATION = "/doctors/me/education";
    private static final String ME_EXPERIENCE = "/doctors/me/experience";
    private static final String ME_PRICE = "/doctors/me/price";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Doctor createDoctor(Specialization specialization) {
        return createDoctor("doctor@example.com", specialization);
    }

    private Doctor createDoctor(String email, Specialization specialization) {
        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR");
        Doctor doctor = Doctor.builder().email(email).password(passwordEncoder.encode("Valid@1234"))
                .firstName("Jane").lastName("Doe").specialization(specialization)
                .price(BigDecimal.valueOf(999)).roles(Set.of(doctorRole)).enabled(true).build();
        doctor.setBiography("Board-certified specialist");
        doctor.setDescription("Outpatient clinician");
        return doctorRepository.save(doctor);
    }

    private Patient createPatient() {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        Patient patient = Patient.builder().email("patient@example.com")
                .password(passwordEncoder.encode("Valid@1234")).firstName("John")
                .lastName("Patient").roles(Set.of(patientRole)).enabled(true).build();
        return patientRepository.save(patient);
    }

    @Nested
    @DisplayName("PUT /doctors/me/education")
    class ReplaceEducation {
        @Test
        @DisplayName("Doctor updates education successfully")
        void shouldPersistEducation_whenDoctorAuthenticated() throws Exception {
            Doctor doctor = createDoctor(Specialization.CARDIOLOGY);
            List<EducationItemRequest> body = List
                    .of(new EducationItemRequest("MD", "Oxford", 2010, 2016));

            mockMvc.perform(put(ME_EDUCATION).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)).with(withUserId(doctor)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.education[0].degree").value("MD"))
                    .andExpect(jsonPath("$.education[0].startYear").value(2010))
                    .andExpect(jsonPath("$.education[0].endYear").value(2016));

            Doctor reloaded = doctorRepository.findById(doctor.getId()).orElseThrow();
            assertThat(reloaded.getEducation()).hasSize(1);
            assertThat(reloaded.getEducation().get(0).getDegree()).isEqualTo("MD");
        }

        @Test
        @DisplayName("Patient is forbidden")
        void shouldRejectPatient() throws Exception {
            Patient patient = createPatient();
            mockMvc.perform(put(ME_EDUCATION).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(List.of())).with(withUserId(patient)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /doctors/me/experience")
    class ReplaceExperience {
        @Test
        @DisplayName("Accepts ongoing role when end year is omitted")
        void shouldAcceptOpenEndedExperience() throws Exception {
            Doctor doctor = createDoctor(Specialization.NEUROLOGY);
            int started = Year.now().getValue() - 4;
            List<ExperienceItemRequest> body = List.of(new ExperienceItemRequest("Attending",
                    "City Hospital", started, null, "ICU rotations"));

            mockMvc.perform(put(ME_EXPERIENCE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)).with(withUserId(doctor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.experience[0].position").value("Attending"))
                    .andExpect(jsonPath("$.experience[0].hospital").value("City Hospital"))
                    .andExpect(jsonPath("$.experience[0].startYear").value(started))
                    .andExpect(jsonPath("$.experience[0].description").value("ICU rotations"));

            Doctor reloaded = doctorRepository.findById(doctor.getId()).orElseThrow();
            assertThat(reloaded.getExperience()).hasSize(1);
            assertThat(reloaded.getExperience().get(0).getEndYear()).isNull();
        }

        @Test
        @DisplayName("Reject start year beyond current calendar year")
        void shouldRejectInvalidStartYear() throws Exception {
            Doctor doctor = createDoctor(Specialization.DENTISTRY);
            List<ExperienceItemRequest> bad = List.of(new ExperienceItemRequest("Resident", "North",
                    Year.now().getValue() + 5, null, null));

            mockMvc.perform(put(ME_EXPERIENCE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bad)).with(withUserId(doctor)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /doctors/me/price")
    class SetPrice {
        @Test
        @DisplayName("Doctor updates consultation price")
        void shouldUpdatePrice() throws Exception {
            Doctor doctor = createDoctor(Specialization.CARDIOLOGY);
            mockMvc.perform(put(ME_PRICE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new SetDoctorPriceRequest(BigDecimal.valueOf(2500))))
                    .with(withUserId(doctor))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(2500));

            assertThat(doctorRepository.findById(doctor.getId()).orElseThrow().getPrice())
                    .isEqualByComparingTo(BigDecimal.valueOf(2500));
        }
    }

    @Nested
    @DisplayName("GET /doctors/{id}")
    class GetDoctor {
        @Test
        @DisplayName("Authenticated user can fetch doctor profile")
        void shouldReturnDoctor() throws Exception {
            Doctor doctor = createDoctor(Specialization.PEDIATRICS);
            doctor.setPersonalPhoto("https://cdn.example.com/doc.jpg");
            doctor = doctorRepository.save(doctor);
            Patient patient = createPatient();

            mockMvc.perform(get("/doctors/" + doctor.getId()).with(withUserId(patient)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Jane Doe"))
                    .andExpect(jsonPath("$.personalPhoto").value("https://cdn.example.com/doc.jpg"))
                    .andExpect(jsonPath("$.specialization").value("PEDIATRICS"))
                    .andExpect(jsonPath("$.price").exists());
        }
    }

    @Nested
    @DisplayName("GET /doctors/specialization/{specialization}")
    class BySpecialization {
        @Test
        @DisplayName("Lists doctors filtered by specialization")
        void shouldPageBySpecialization() throws Exception {
            createDoctor("d1@test.com", Specialization.ENDOCRINOLOGY);
            createDoctor("d2@test.com", Specialization.ENDOCRINOLOGY);
            Patient patient = createPatient();

            mockMvc.perform(
                    get("/doctors/specialization/ENDOCRINOLOGY?size=10").with(withUserId(patient)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(2));
        }
    }
}
