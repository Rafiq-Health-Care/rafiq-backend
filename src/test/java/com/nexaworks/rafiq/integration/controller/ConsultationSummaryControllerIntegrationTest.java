package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
import com.nexaworks.rafiq.dto.request.summary.CreateConsultationSummaryRequest;
import com.nexaworks.rafiq.dto.request.summary.UpdateConsultationSummaryRequest;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.CancellationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;
import com.nexaworks.rafiq.repository.ConsultationSummaryRepository;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.UserRepository;

@DisplayName("Consultation summary API integration tests")
public class ConsultationSummaryControllerIntegrationTest extends BaseIntegrationTest {

    private static final String BASE = "/consultation-summary";

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
    private ConsultationSummaryRepository consultationSummaryRepository;
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
        consultationSummaryRepository.deleteAll();
        cancellationLogRepository.deleteAll();
        consultationRepository.deleteAll();
        consultationSlotRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Doctor createDoctor() {
        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR");
        Doctor doctor = Doctor.builder().email("d@example.com")
                .password(passwordEncoder.encode("Valid@1234")).firstName("Jane").lastName("Doe")
                .specialization(Specialization.CARDIOLOGY).price(BigDecimal.valueOf(150))
                .roles(Set.of(doctorRole)).enabled(true).build();
        return doctorRepository.save(doctor);
    }

    private Patient createPatient() {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        Patient patient = Patient.builder().email("p@example.com")
                .password(passwordEncoder.encode("Valid@1234")).firstName("John").lastName("Smith")
                .roles(Set.of(patientRole)).enabled(true).build();
        return patientRepository.save(patient);
    }

    private Consultation persistConsultation(Doctor doctor, Patient patient,
            ConsultationStatus status) {
        return persistConsultation(doctor, patient, status,
                LocalDateTime.now().plusDays(1).withNano(0));
    }

    private Consultation persistConsultation(Doctor doctor, Patient patient,
            ConsultationStatus status, LocalDateTime start) {
        ConsultationSlot slot = ConsultationSlot.builder().doctor(doctor).startTime(start)
                .endTime(start.plusMinutes(60)).durationMinutes(60).status(SlotStatus.BOOKED)
                .build();
        ConsultationSlot savedSlot = consultationSlotRepository.save(slot);
        Consultation c = Consultation.builder().slot(savedSlot).patient(patient).status(status)
                .build();
        return consultationRepository.save(c);
    }

    private CreateConsultationSummaryRequest createRequest(UUID consultationId) {
        return new CreateConsultationSummaryRequest(consultationId, "Visit summary", "Rest well",
                List.of(), List.of("CBC"));
    }

    @Nested
    class CreateSummary {
        @Test
        void doctorCreatesSummary_whenCompleted() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation c = persistConsultation(doctor, patient, ConsultationStatus.COMPLETED);

            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(c.getId())))
                    .with(withUserId(doctor))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.summary").value("Visit summary"))
                    .andExpect(jsonPath("$.consultationId").value(c.getId().toString()));

            assertThat(consultationSummaryRepository.count()).isEqualTo(1);
        }

        @Test
        void doctorRejected_whenConsultationNotCompleted() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation c = persistConsultation(doctor, patient, ConsultationStatus.UPCOMING);

            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(c.getId())))
                    .with(withUserId(doctor))).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetAndList {
        @Test
        void patientAndDoctorCanGet_whenParticipant() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation c = persistConsultation(doctor, patient, ConsultationStatus.COMPLETED);
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(c.getId())))
                    .with(withUserId(doctor))).andExpect(status().isOk());

            UUID summaryId = consultationSummaryRepository.findAll().get(0).getId();

            mockMvc.perform(get(BASE + "/" + summaryId).with(withUserId(patient)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.id").exists());

            mockMvc.perform(get(BASE + "/" + summaryId).with(withUserId(doctor)))
                    .andExpect(status().isOk());
        }

        @Test
        void patientListsOwnSummaries() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation c = persistConsultation(doctor, patient, ConsultationStatus.COMPLETED);
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(c.getId())))
                    .with(withUserId(doctor))).andExpect(status().isOk());

            mockMvc.perform(
                    get(BASE).param("page", "0").param("size", "10").with(withUserId(patient)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        void doctorForbidden_listWithoutPatientId() throws Exception {
            Doctor doctor = createDoctor();
            mockMvc.perform(
                    get(BASE).param("page", "0").param("size", "10").with(withUserId(doctor)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void doctorListsPatientSummaries_whenGateConsultationActive() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation completed = persistConsultation(doctor, patient,
                    ConsultationStatus.COMPLETED, LocalDateTime.now().plusDays(1).withNano(0));
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(completed.getId())))
                    .with(withUserId(doctor))).andExpect(status().isOk());
            persistConsultation(doctor, patient, ConsultationStatus.UPCOMING,
                    LocalDateTime.now().plusDays(2).withNano(0));

            mockMvc.perform(get(BASE).param("page", "0").param("size", "10")
                    .param("patientId", patient.getId().toString()).with(withUserId(doctor)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        void doctorForbidden_listWhenOnlyTerminalConsultations() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation completed = persistConsultation(doctor, patient,
                    ConsultationStatus.COMPLETED, LocalDateTime.now().plusDays(1).withNano(0));
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(completed.getId())))
                    .with(withUserId(doctor))).andExpect(status().isOk());
            persistConsultation(doctor, patient, ConsultationStatus.COMPLETED,
                    LocalDateTime.now().plusDays(2).withNano(0));

            mockMvc.perform(get(BASE).param("page", "0").param("size", "10")
                    .param("patientId", patient.getId().toString()).with(withUserId(doctor)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateDelete {
        @Test
        void doctorUpdates() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation c = persistConsultation(doctor, patient, ConsultationStatus.COMPLETED);
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(c.getId())))
                    .with(withUserId(doctor))).andExpect(status().isOk());
            UUID summaryId = consultationSummaryRepository.findAll().get(0).getId();
            UpdateConsultationSummaryRequest upd = new UpdateConsultationSummaryRequest("Updated",
                    null, null, null);

            mockMvc.perform(put(BASE + "/" + summaryId).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(upd)).with(withUserId(doctor)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.summary").value("Updated"));
        }

        @Test
        void patientDeletes() throws Exception {
            Doctor doctor = createDoctor();
            Patient patient = createPatient();
            Consultation c = persistConsultation(doctor, patient, ConsultationStatus.COMPLETED);
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest(c.getId())))
                    .with(withUserId(doctor))).andExpect(status().isOk());
            UUID summaryId = consultationSummaryRepository.findAll().get(0).getId();

            mockMvc.perform(delete(BASE + "/" + summaryId).with(withUserId(patient)))
                    .andExpect(status().isNoContent());
        }
    }
}
