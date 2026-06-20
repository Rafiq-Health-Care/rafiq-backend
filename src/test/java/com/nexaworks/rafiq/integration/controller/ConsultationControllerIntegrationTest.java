package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.CancelConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.EditConsultationSlotRequest;
import com.nexaworks.rafiq.dto.request.consultation.ReserveConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.Payment;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.CancellationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.repository.PaymentRepository;
import com.nexaworks.rafiq.repository.RefundRepository;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.payment.PaymentService;

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
    private ConsultationSlotRepository consultationSlotRepository;

    @Autowired
    private CancellationLogRepository cancellationLogRepository;

    @Autowired
    private ConsultationLogRepository consultationLogRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PaymentService paymentService;

    @BeforeEach
    void cleanDatabase() {
        cancellationLogRepository.deleteAll();
        consultationLogRepository.deleteAll();
        refundRepository.deleteAll();
        paymentRepository.deleteAll();
        consultationRepository.deleteAll();
        consultationSlotRepository.deleteAll();
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
                .price(BigDecimal.valueOf(150)).roles(Set.of(doctorRole)).enabled(true)
                .birthDate(LocalDate.from(LocalDateTime.of(1977, 1, 1, 0, 0))).build();
        return doctorRepository.save(doctor);
    }

    private Patient createPatient() {
        return createPatient("patient@example.com");
    }

    private Patient createPatient(String email) {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        Patient patient = Patient.builder().email(email)
                .password(passwordEncoder.encode("Valid@1234")).firstName("John").lastName("Smith")
                .roles(Set.of(patientRole)).enabled(true)
                .birthDate(LocalDate.from(LocalDateTime.of(1977, 1, 1, 0, 0))).build();
        return patientRepository.save(patient);
    }

    private ConsultationSlot persistSlot(Doctor doctor, SlotStatus status,
            LocalDateTime startTime) {
        ConsultationSlot slot = ConsultationSlot.builder().doctor(doctor).startTime(startTime)
                .endTime(startTime.plusMinutes(60)).durationMinutes(60).status(status).build();
        return consultationSlotRepository.save(slot);
    }

    private Consultation persistConsultation(ConsultationSlot slot, Patient patient,
            ConsultationStatus status) {
        Consultation consultation = Consultation.builder().slot(slot).patient(patient)
                .doctor(slot.getDoctor()).status(status).build();
        return consultationRepository.save(consultation);
    }

    private Payment persistPayment(Consultation consultation, Patient patient) {
        Payment payment = Payment.builder().consultation(consultation).patient(patient)
                .paymentIntentId("pi_test_" + consultation.getId())
                .clientSecret("secret_" + consultation.getId()).amount(BigDecimal.valueOf(150))
                .currency("USD").status(PaymentStatus.SUCCEEDED)
                .paymentProvider(PaymentProvider.STRIPE).build();
        Payment savedPayment = paymentRepository.saveAndFlush(payment);
        consultation.setPayment(savedPayment);
        consultationRepository.saveAndFlush(consultation);
        return savedPayment;
    }

    @Nested
    @DisplayName("POST /slot")
    class AddConsultation {
        private static final String ENDPOINT = "/api/v1/slot";

        @Test
        @DisplayName("Doctor creates a consultation successfully")
        void shouldCreateConsultation_WhenDoctorIsAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            AddConsultationRequest request = new AddConsultationRequest(
                    LocalDateTime.now().plusDays(2).withNano(0), 60);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .header(IDEMPOTENCY_KEY_HEADER, UUID.randomUUID())
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(doctor)))
                    .andExpect(status().isCreated());

            assertThat(consultationSlotRepository.count()).isEqualTo(1);
            ConsultationSlot slot = consultationSlotRepository.findAll().get(0);
            assertThat(slot.getDoctor().getId()).isEqualTo(doctor.getId());
            assertThat(slot.getDurationMinutes()).isEqualTo(60);
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        }

        @Test
        @DisplayName("A patient is forbidden from creating a consultation")
        void shouldReturnForbidden_WhenPatientTriesToAdd() throws Exception {
            Patient patient = createPatient();
            AddConsultationRequest request = new AddConsultationRequest(
                    LocalDateTime.now().plusDays(2), 60);

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .header(IDEMPOTENCY_KEY_HEADER, UUID.randomUUID())
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(patient)))
                    .andExpect(status().isForbidden());

            assertThat(consultationRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("POST /slot/schedule/search")
    class GetSchedule {
        private static final String ENDPOINT = "/api/v1/slot/schedule/search";

        @Test
        @DisplayName(" Doctor retrieves their own schedule")
        void shouldReturnDoctorSchedule_WhenDoctorIsAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            persistSlot(doctor, SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));
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
    @DisplayName("PUT /slot/{id}")
    class EditConsultation {
        private static final String ENDPOINT = "/api/v1/slot/{id}";

        @Test
        @DisplayName("Doctor edits their own AVAILABLE consultation")
        void shouldEditConsultation_WhenStatusIsAvailableAndDoctorOwnsIt() throws Exception {
            Doctor doctor = createDoctor();
            ConsultationSlot slot = persistSlot(doctor, SlotStatus.AVAILABLE,
                    LocalDateTime.now().plusDays(1));
            LocalDateTime newStart = LocalDateTime.now().plusDays(3).withNano(0).withSecond(0);
            EditConsultationSlotRequest request = new EditConsultationSlotRequest(newStart,
                    newStart.plusMinutes(30), 30, SlotStatus.AVAILABLE);

            mockMvc.perform(put(ENDPOINT, slot.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(doctor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.slotId").value(slot.getId().toString()));

            ConsultationSlot updated = consultationSlotRepository.findById(slot.getId())
                    .orElseThrow();
            assertThat(updated.getStartTime()).isEqualTo(newStart);
            assertThat(updated.getEndTime()).isEqualTo(newStart.plusMinutes(30));
        }

        @Test
        @DisplayName("Editing a non-existent consultation throws ConsultationException")
        void shouldThrow_WhenConsultationDoesNotExist() throws Exception {
            Doctor doctor = createDoctor();
            EditConsultationSlotRequest request = new EditConsultationSlotRequest(
                    LocalDateTime.now().plusDays(3),
                    LocalDateTime.now().plusDays(3).plusMinutes(30), 30, SlotStatus.AVAILABLE);

            mockMvc.perform(put(ENDPOINT, UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(doctor)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /consultation/{id}/cancel")
    class CancelConsultation {
        private static final String ENDPOINT = "/api/v1/consultation/{id}/cancel";

        @Test
        @DisplayName("Patient cancels their UPCOMING consultation")
        @Transactional
        void shouldCancelUpcomingConsultation_WhenPatientIsOwner() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            ConsultationSlot slot = persistSlot(doctor, SlotStatus.BOOKED,
                    LocalDateTime.now().plusDays(1));
            Consultation consultation = persistConsultation(slot, patient,
                    ConsultationStatus.UPCOMING);
            persistPayment(consultation, patient);

            CancelConsultationRequest request = new CancelConsultationRequest(
                    "scheduling conflict");
            mockMvc.perform(patch(ENDPOINT, consultation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(patient)))
                    .andExpect(status().isOk());

            Consultation cancelled = consultationRepository.findById(consultation.getId())
                    .orElseThrow();
            assertThat(cancelled.getStatus()).isEqualTo(ConsultationStatus.CANCELLED);
            ConsultationSlot updatedSlot = consultationSlotRepository.findById(slot.getId())
                    .orElseThrow();
            assertThat(updatedSlot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Cancelling a non-existent consultation throws ConsultationException")
        void shouldThrow_WhenConsultationDoesNotExist() throws Exception {
            Doctor doctor = createDoctor();
            CancelConsultationRequest request = new CancelConsultationRequest("any");

            mockMvc.perform(patch(ENDPOINT, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(doctor)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /consultation")
    class ReserveConsultation {
        private static final String ENDPOINT = "/api/v1/consultation";

        @Test
        @DisplayName(" Patient reserves an AVAILABLE consultation")
        void shouldReserveConsultation_WhenStatusIsAvailable() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            ConsultationSlot slot = persistSlot(doctor, SlotStatus.AVAILABLE,
                    LocalDateTime.now().plusDays(1));
            ReserveConsultationRequest request = new ReserveConsultationRequest(slot.getId(),
                    "notes", PaymentProvider.STRIPE);

            org.mockito.Mockito
                    .when(paymentService.process(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq(patient),
                            org.mockito.ArgumentMatchers.eq(PaymentProvider.STRIPE)))
                    .thenReturn("pi_secret_xyz");

            mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", "idempotency-key")
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(patient)))
                    .andExpect(status().isCreated());

            ConsultationSlot updatedSlot = consultationSlotRepository.findById(slot.getId())
                    .orElseThrow();
            assertThat(updatedSlot.getStatus()).isEqualTo(SlotStatus.PENDING_PAYMENT);
            assertThat(consultationRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("GET /consultation/{id}")
    class GetConsultation {
        private static final String ENDPOINT = "/api/v1/consultation/{id}";

        @Test
        @DisplayName("Authenticated user retrieves an existing consultation")
        void shouldReturnConsultation_WhenItExists() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            ConsultationSlot slot = persistSlot(doctor, SlotStatus.BOOKED,
                    LocalDateTime.now().plusDays(1));
            Consultation consultation = persistConsultation(slot, patient,
                    ConsultationStatus.UPCOMING);

            mockMvc.perform(get(ENDPOINT, consultation.getId()).with(withUserId(patient)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.consultationId").value(consultation.getId().toString()))
                    .andExpect(jsonPath("$.status").value("UPCOMING"))
                    .andExpect(jsonPath("$.doctor.id").value(doctor.getId().toString()))
                    .andExpect(jsonPath("$.slotId").value(slot.getId().toString()));
        }

        @Test
        @DisplayName("Fetching a non-existent consultation throws ConsultationException")
        void shouldThrow_WhenConsultationDoesNotExist() throws Exception {
            Patient patient = createPatient();
            mockMvc.perform(get(ENDPOINT, UUID.randomUUID()).with(withUserId(patient)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /consultations/{id}/call/enter")
    class GetCall {
        private static final String ENDPOINT = "/api/v1/consultations/{id}/call/enter";

        @Test
        @DisplayName("Authenticated user retrieves call info for a consultation")
        void shouldReturnCallInfo_WhenAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            ConsultationSlot slot = persistSlot(doctor, SlotStatus.BOOKED,
                    LocalDateTime.now().plusDays(1));
            Consultation consultation = persistConsultation(slot, patient,
                    ConsultationStatus.UPCOMING);

            mockMvc.perform(post(ENDPOINT, consultation.getId())
                    .header(IDEMPOTENCY_KEY_HEADER, UUID.randomUUID()).with(withUserId(patient)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unauthenticated request is rejected with 401")
        void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post(ENDPOINT, UUID.randomUUID())).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /slot/doctor/{id}")
    class FilterConsultations {
        private static final String ENDPOINT = "/api/v1/slot/doctor/{id}";

        @Test
        @DisplayName("Authenticated user fetches available slots for doctor")
        void shouldReturnAvailableSlots_WhenAuthenticated() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            persistSlot(doctor, SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));

            mockMvc.perform(get(ENDPOINT, doctor.getId()).with(withUserId(patient)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        @DisplayName("Unauthenticated request is rejected with 401")
        void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get(ENDPOINT, UUID.randomUUID())).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /consultation/patient/{status}")
    class PatientUpcoming {
        private static final String ENDPOINT = "/api/v1/consultation/patient/{status}";

        @Test
        @DisplayName(" Patient retrieves their upcoming consultations")
        void shouldReturnUpcomingConsultations_WhenAuthenticatedAsPatient() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            ConsultationSlot slot = persistSlot(doctor, SlotStatus.BOOKED,
                    LocalDateTime.now().plusDays(1));
            persistConsultation(slot, patient, ConsultationStatus.UPCOMING);

            mockMvc.perform(get(ENDPOINT, ConsultationStatus.UPCOMING).with(withUserId(patient)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        @DisplayName("Doctor is forbidden from accessing the patient upcoming endpoint")
        void shouldReturnForbidden_WhenAuthenticatedAsDoctor() throws Exception {
            Doctor doctor = createDoctor();

            mockMvc.perform(get(ENDPOINT, ConsultationStatus.UPCOMING).with(withUserId(doctor)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /slot/doctor/upcoming")
    class DoctorUpcoming {
        private static final String ENDPOINT = "/api/v1/slot/doctor/upcoming";

        @Test
        @DisplayName("Doctor retrieves their upcoming consultations")
        void shouldReturnUpcomingConsultations_WhenAuthenticatedAsDoctor() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            ConsultationSlot slot = persistSlot(doctor, SlotStatus.BOOKED,
                    LocalDateTime.now().plusDays(1));
            persistConsultation(slot, patient, ConsultationStatus.UPCOMING);

            mockMvc.perform(get(ENDPOINT).with(withUserId(doctor))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("UPCOMING"));
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
