package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationFilter;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.TimeSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.CancellationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.UserRepository;

@DisplayName("Consultation Controller Integration Tests")
public class ConsultationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private CancellationLogRepository cancellationLogRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        cancellationLogRepository.deleteAll();
        consultationRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Doctor createDoctor() {
        return createDoctor("doctor@example.com");
    }

    private Doctor createDoctor(String email) {
        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR");
        Doctor doctor = Doctor.builder().email(email).password(passwordEncoder.encode("Valid@1234"))
                .firstName("Jane").lastName("Doe").specialization(Specialization.CARDIOLOGY)
                .price(BigDecimal.valueOf(150)).roles(Set.of(doctorRole)).enabled(true).build();
        return doctorRepository.save(doctor);
    }

    private Patient createPatient() {
        return createPatient("patient@example.com");
    }

    private Patient createPatient(String email) {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        Patient patient = Patient.builder().email(email)
                .password(passwordEncoder.encode("Valid@1234")).firstName("John").lastName("Smith")
                .roles(Set.of(patientRole)).enabled(true).build();
        return patientRepository.save(patient);
    }

    private Consultation persistConsultation(Doctor doctor, ConsultationStatus status) {
        return persistConsultation(doctor, null, status, LocalDateTime.now().plusDays(1));
    }

    private Consultation persistConsultation(Doctor doctor, Patient patient,
            ConsultationStatus status, LocalDateTime startTime) {
        TimeSlot slot = TimeSlot.builder().startTime(startTime).endTime(startTime.plusMinutes(60))
                .durationMinutes(60).build();
        Consultation consultation = Consultation.builder().doctor(doctor).patient(patient)
                .timeSlot(slot).status(status).specialization(doctor.getSpecialization()).build();
        return consultationRepository.save(consultation);
    }

    @Nested
    @DisplayName("POST /consultation/add")
    class AddConsultation {
        private static final String ENDPOINT = "/consultation/add";

        @Test
        @DisplayName("Doctor creates a consultation successfully")
        void shouldCreateConsultation_WhenDoctorIsAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            AddConsultationRequest request = new AddConsultationRequest(
                    LocalDateTime.now().plusDays(2).withNano(0), 60);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(doctor)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.duration").value(60))
                    .andExpect(jsonPath("$.price").value(150))
                    .andExpect(jsonPath("$.status").value("AVAILABLE"))
                    .andExpect(jsonPath("$.doctor.id").value(doctor.getId().toString()));

            assertThat(consultationRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("A patient is forbidden from creating a consultation")
        void shouldReturnForbidden_WhenPatientTriesToAdd() throws Exception {
            Patient patient = createPatient();
            AddConsultationRequest request = new AddConsultationRequest(
                    LocalDateTime.now().plusDays(2), 60);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(patient)))
                    .andExpect(status().isForbidden());

            assertThat(consultationRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("POST /consultation/schedule")
    class GetSchedule {
        private static final String ENDPOINT = "/consultation/schedule";

        @Test
        @DisplayName(" Doctor retrieves their own schedule")
        void shouldReturnDoctorSchedule_WhenDoctorIsAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            persistConsultation(doctor, ConsultationStatus.AVAILABLE);
            ScheduleFilter filter = new ScheduleFilter(null, null, null);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filter)).with(withUserId(doctor)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));
        }

        @Test
        @DisplayName("Patient is forbidden from accessing the doctor schedule endpoint")
        void shouldReturnForbidden_WhenPatientCallsScheduleEndpoint() throws Exception {
            Patient patient = createPatient();
            ScheduleFilter filter = new ScheduleFilter(null, null, null);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filter)).with(withUserId(patient)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /consultation/edit/{id}")
    class EditConsultation {
        private static final String ENDPOINT = "/consultation/edit/{id}";

        @Test
        @DisplayName("Doctor edits their own AVAILABLE consultation")
        void shouldEditConsultation_WhenStatusIsAvailableAndDoctorOwnsIt() throws Exception {
            Doctor doctor = createDoctor();
            Consultation consultation = persistConsultation(doctor, ConsultationStatus.AVAILABLE);
            LocalDateTime newStart = LocalDateTime.now().plusDays(3).withNano(0).withSecond(0);
            AddConsultationRequest request = new AddConsultationRequest(newStart, 30);

            mockMvc.perform(put(ENDPOINT, consultation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(doctor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(consultation.getId().toString()));

            Consultation updated = consultationRepository.findById(consultation.getId())
                    .orElseThrow();
            assertThat(updated.getTimeSlot().getStartTime()).isEqualTo(newStart);
            assertThat(updated.getTimeSlot().getEndTime()).isEqualTo(newStart.plusMinutes(30));
        }

        @Test
        @DisplayName("Editing a non-existent consultation throws ConsultationException")
        void shouldThrow_WhenConsultationDoesNotExist() throws Exception {
            Doctor doctor = createDoctor();
            AddConsultationRequest request = new AddConsultationRequest(
                    LocalDateTime.now().plusDays(3), 30);

            assertThatThrownBy(() -> mockMvc.perform(put(ENDPOINT, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(doctor))))
                    .hasCauseInstanceOf(ConsultationException.class)
                    .hasMessageContaining("Consultation not found");
        }
    }

    @Nested
    @DisplayName("PATCH /consultation/cancel/{id}")
    class CancelConsultation {
        private static final String ENDPOINT = "/consultation/cancel/{id}";

        @Test
        @DisplayName("Doctor cancels their AVAILABLE consultation")
        void shouldCancelAvailableConsultation_WhenDoctorIsOwner() throws Exception {
            Doctor doctor = createDoctor();
            Consultation consultation = persistConsultation(doctor, ConsultationStatus.AVAILABLE);

            mockMvc.perform(patch(ENDPOINT, consultation.getId())
                    .param("reason", "scheduling conflict").with(withUserId(doctor)))
                    .andExpect(status().isOk());

            Consultation cancelled = consultationRepository.findById(consultation.getId())
                    .orElseThrow();
            assertThat(cancelled.getStatus()).isEqualTo(ConsultationStatus.CANCELLED);
        }

        @Test
        @DisplayName("Cancelling a non-existent consultation throws ConsultationException")
        void shouldThrow_WhenConsultationDoesNotExist() throws Exception {
            Doctor doctor = createDoctor();

            assertThatThrownBy(() -> mockMvc.perform(patch(ENDPOINT, UUID.randomUUID())
                    .param("reason", "any").with(withUserId(doctor))))
                    .hasCauseInstanceOf(ConsultationException.class)
                    .hasMessageContaining("Consultation not found");
        }
    }

    @Nested
    @DisplayName("POST /consultation/reserve/{id}")
    class ReserveConsultation {
        private static final String ENDPOINT = "/consultation/reserve/{id}";

        @Test
        @DisplayName(" Patient reserves an AVAILABLE consultation")
        void shouldReserveConsultation_WhenStatusIsAvailable() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation consultation = persistConsultation(doctor, ConsultationStatus.AVAILABLE);

            mockMvc.perform(post(ENDPOINT, consultation.getId())
                    .param("provider", PaymentProvider.STRIPE.name()).with(withUserId(patient)))
                    .andExpect(status().isOk());

            Consultation reserved = consultationRepository.findById(consultation.getId())
                    .orElseThrow();
            assertThat(reserved.getStatus()).isEqualTo(ConsultationStatus.BOOKED);
            assertThat(reserved.getPatient().getId()).isEqualTo(patient.getId());
        }

        @Test
        @DisplayName("A doctor is forbidden from reserving a consultation")
        void shouldReturnForbidden_WhenDoctorTriesToReserve() throws Exception {
            Doctor doctor = createDoctor();
            Doctor otherDoctor = createDoctor("other-doctor@example.com");
            Consultation consultation = persistConsultation(otherDoctor,
                    ConsultationStatus.AVAILABLE);

            mockMvc.perform(post(ENDPOINT, consultation.getId())
                    .param("provider", PaymentProvider.STRIPE.name()).with(withUserId(doctor)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /consultation/{id}")
    class GetConsultation {
        private static final String ENDPOINT = "/consultation/{id}";

        @Test
        @DisplayName("Authenticated user retrieves an existing consultation")
        void shouldReturnConsultation_WhenItExists() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation consultation = persistConsultation(doctor, ConsultationStatus.AVAILABLE);

            mockMvc.perform(get(ENDPOINT, consultation.getId()).with(withUserId(patient)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(consultation.getId().toString()))
                    .andExpect(jsonPath("$.status").value("AVAILABLE"))
                    .andExpect(jsonPath("$.doctor.id").value(doctor.getId().toString()));
        }

        @Test
        @DisplayName("Fetching a non-existent consultation throws ConsultationException")
        void shouldThrow_WhenConsultationDoesNotExist() throws Exception {
            Patient patient = createPatient();

            assertThatThrownBy(() -> mockMvc
                    .perform(get(ENDPOINT, UUID.randomUUID()).with(withUserId(patient))))
                    .hasCauseInstanceOf(ConsultationException.class)
                    .hasMessageContaining("Consultation not found");
        }
    }

    @Nested
    @DisplayName("GET /consultation/{id}/call")
    class GetCall {
        private static final String ENDPOINT = "/consultation/{id}/call";

        @Test
        @DisplayName("Authenticated user retrieves call info for a consultation")
        void shouldReturnCallInfo_WhenAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation consultation = persistConsultation(doctor, ConsultationStatus.AVAILABLE);

            mockMvc.perform(get(ENDPOINT, consultation.getId()).with(withUserId(patient)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unauthenticated request is rejected with 401")
        void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get(ENDPOINT, UUID.randomUUID())).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /consultation/filter")
    class FilterConsultations {
        private static final String ENDPOINT = "/consultation/filter";

        @Test
        @DisplayName("Authenticated user filters consultations and gets paged result")
        void shouldReturnFilteredConsultations_WhenAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            persistConsultation(doctor, ConsultationStatus.AVAILABLE);
            ConsultationFilter filter = new ConsultationFilter(doctor.getId(),
                    Specialization.CARDIOLOGY, null, null, null, null);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filter)).with(withUserId(patient)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        @DisplayName(" Unauthenticated filter request is rejected with 401")
        void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
            ConsultationFilter filter = new ConsultationFilter(null, null, null, null, null, null);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filter)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /consultation/patient/upcoming")
    class PatientUpcoming {
        private static final String ENDPOINT = "/consultation/patient/upcoming";

        @Test
        @DisplayName(" Patient retrieves their upcoming consultations")
        void shouldReturnUpcomingConsultations_WhenAuthenticatedAsPatient() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            persistConsultation(doctor, patient, ConsultationStatus.CONFIRMED,
                    LocalDateTime.now().plusDays(1));

            mockMvc.perform(get(ENDPOINT).with(withUserId(patient))).andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Doctor is forbidden from accessing the patient upcoming endpoint")
        void shouldReturnForbidden_WhenAuthenticatedAsDoctor() throws Exception {
            Doctor doctor = createDoctor();

            mockMvc.perform(get(ENDPOINT).with(withUserId(doctor)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /consultation/doctor/upcoming")
    class DoctorUpcoming {
        private static final String ENDPOINT = "/consultation/doctor/upcoming";

        @Test
        @DisplayName("Doctor retrieves their upcoming consultations")
        void shouldReturnUpcomingConsultations_WhenAuthenticatedAsDoctor() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            persistConsultation(doctor, patient, ConsultationStatus.CONFIRMED,
                    LocalDateTime.now().plusDays(1));

            mockMvc.perform(get(ENDPOINT).with(withUserId(doctor))).andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("Patient is forbidden from accessing the doctor upcoming endpoint")
        void shouldReturnForbidden_WhenAuthenticatedAsPatient() throws Exception {
            Patient patient = createPatient();

            mockMvc.perform(get(ENDPOINT).with(withUserId(patient)))
                    .andExpect(status().isForbidden());
        }
    }
}
