package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.reminder.AddReminderRequest;
import com.nexaworks.rafiq.dto.request.reminder.ReminderFilters;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.entities.enums.Gender;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.MedicineType;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.*;

import jakarta.persistence.EntityManager;

@DisplayName("Reminder Controller Integration Test Cases")
public class ReminderControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ReminderRepository reminderRepository;
    @Autowired
    MedicineRepository medicineRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    DrugRepository drugRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ReminderLogRepository reminderLogRepository;
    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        reminderLogRepository.deleteAll();
        reminderRepository.deleteAll();
        medicineRepository.deleteAll();
        patientRepository.deleteAll();
        drugRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createTestUser() {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        // Create Patient directly (Patient extends User with is-a relationship)
        Patient patient = Patient.builder().email("test@example.com")
                .password(passwordEncoder.encode("Valid@1234")).firstName("John").lastName("Doe")
                .phone("+12345678901").birthDate(LocalDate.of(1999, 1, 1)).gender(Gender.MALE)
                .roles(Set.of(patientRole)).enabled(true).build();
        return patientRepository.save(patient);
    }

    private Medicine createMedicineForUser(User user) {
        Drug drug = Drug.builder().tradeName("Test Drug").drugGroup("NSAIDs").dosageForm("Tablet")
                .route("Oral").price(10.0).build();
        drugRepository.save(drug);

        Medicine medicine = Medicine.builder().patient((Patient) user).drug(drug)
                .name("Test Medicine").dosage("10mg").frequency(MedicineFrequency.ONCE)
                .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30)).build();
        return medicineRepository.save(medicine);
    }

    @Nested
    @DisplayName("POST /reminder/create - Create Reminder")
    class CreateReminder {
        private static final String CREATE_REMINDER_ENDPOINT = "/reminder/create";

        @Test
        void shouldCreateReminder_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            AddReminderRequest request = new AddReminderRequest(medicine.getId(), true,
                    LocalDateTime.now().plusHours(2));

            mockMvc.perform(MockMvcRequestBuilders.post(CREATE_REMINDER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Reminder created successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.medicineId")
                            .value(medicine.getId().toString()));
        }

        @Test
        void shouldReturnBadRequest_WhenMedicineIdIsInvalid() throws Exception {
            User user = createTestUser();

            AddReminderRequest request = new AddReminderRequest(UUID.randomUUID(), true,
                    LocalDateTime.now().plusHours(2));

            mockMvc.perform(MockMvcRequestBuilders.post(CREATE_REMINDER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldCreateReminder_WithVibrationDisabled() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            AddReminderRequest request = new AddReminderRequest(medicine.getId(), false,
                    LocalDateTime.now().plusHours(1));

            mockMvc.perform(MockMvcRequestBuilders.post(CREATE_REMINDER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.vibrate").value(false));
        }
    }

    @Nested
    @DisplayName("GET /reminder/history - Get Reminder History")
    class GetReminderHistory {
        private static final String GET_HISTORY_ENDPOINT = "/reminder/history";

        @Test
        void shouldGetReminderHistory_WithFilters() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            ReminderLog log = ReminderLog.builder().reminder(reminder).patient((Patient) user)
                    .status(ReminderStatus.TAKEN).timestamp(LocalDateTime.now()).build();
            reminderLogRepository.save(log);

            ReminderFilters filters = new ReminderFilters(null, null, null, ReminderStatus.TAKEN);

            mockMvc.perform(MockMvcRequestBuilders.get(GET_HISTORY_ENDPOINT).param("page", "0")
                    .param("size", "10").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filters)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());
        }

        @Test
        void shouldReturnEmptyHistory_WhenNoReminders() throws Exception {
            User user = createTestUser();

            ReminderFilters filters = new ReminderFilters(null, null, null, null);

            mockMvc.perform(MockMvcRequestBuilders.get(GET_HISTORY_ENDPOINT).param("page", "0")
                    .param("size", "10").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filters)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());
        }

        @Test
        void shouldGetHistory_OnlyForAuthenticatedUser() throws Exception {
            User user1 = createTestUser();
            Medicine medicine1 = createMedicineForUser(user1);

            Reminder reminder1 = Reminder.builder().patient((Patient) user1).medicine(medicine1)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder1);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient patient2 = Patient.builder().email("user2@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Jane").lastName("Doe")
                    .roles(Set.of(patientRole)).enabled(true).build();
            User user2 = patientRepository.save(patient2);

            ReminderFilters filters = new ReminderFilters(null, null, null, null);

            mockMvc.perform(MockMvcRequestBuilders.get(GET_HISTORY_ENDPOINT).param("page", "0")
                    .param("size", "10").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filters)).with(withUserId(user2)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /reminder/taken/{reminder-id} - Mark Reminder as Taken")
    class MarkReminderAsTaken {
        private static final String MARK_TAKEN_ENDPOINT = "/reminder/taken/{reminder-id}";

        @Test
        void shouldMarkReminderAsTaken_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            LocalDateTime takenTime = LocalDateTime.now();

            mockMvc.perform(MockMvcRequestBuilders.post(MARK_TAKEN_ENDPOINT, reminder.getId())
                    .param("taken-time", takenTime.toString()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            assertThat(reminderLogRepository.findAll()).hasSize(1);
        }

        @Test
        void shouldReturnNotFound_WhenReminderDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();
            LocalDateTime takenTime = LocalDateTime.now();

            mockMvc.perform(MockMvcRequestBuilders.post(MARK_TAKEN_ENDPOINT, nonExistentId)
                    .param("taken-time", takenTime.toString()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnNotFound_WhenUserDoesNotOwnReminder() throws Exception {
            User owner = createTestUser();
            Medicine medicine = createMedicineForUser(owner);

            Reminder reminder = Reminder.builder().patient((Patient) owner).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient otherPatient = Patient.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).enabled(true).build();
            User otherUser = patientRepository.save(otherPatient);

            LocalDateTime takenTime = LocalDateTime.now();

            mockMvc.perform(MockMvcRequestBuilders.post(MARK_TAKEN_ENDPOINT, reminder.getId())
                    .param("taken-time", takenTime.toString()).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /reminder/missed/{reminder-id} - Mark Reminder as Missed")
    class MarkReminderAsMissed {
        private static final String MARK_MISSED_ENDPOINT = "/reminder/missed/{reminder-id}";

        @Test
        void shouldMarkReminderAsMissed_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            LocalDateTime missedTime = LocalDateTime.now();

            mockMvc.perform(MockMvcRequestBuilders.post(MARK_MISSED_ENDPOINT, reminder.getId())
                    .param("taken-time", missedTime.toString()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            assertThat(reminderLogRepository.findAll()).hasSize(1);
        }

        @Test
        void shouldReturnNotFound_WhenReminderDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();
            LocalDateTime missedTime = LocalDateTime.now();

            mockMvc.perform(MockMvcRequestBuilders.post(MARK_MISSED_ENDPOINT, nonExistentId)
                    .param("taken-time", missedTime.toString()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnNotFound_WhenUserDoesNotOwnReminder() throws Exception {
            User owner = createTestUser();
            Medicine medicine = createMedicineForUser(owner);

            Reminder reminder = Reminder.builder().patient((Patient) owner).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient otherPatient = Patient.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).enabled(true).build();
            User otherUser = patientRepository.save(otherPatient);

            LocalDateTime missedTime = LocalDateTime.now();

            mockMvc.perform(MockMvcRequestBuilders.post(MARK_MISSED_ENDPOINT, reminder.getId())
                    .param("taken-time", missedTime.toString()).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /reminder/all - Get All Reminders")
    class GetAllReminders {
        private static final String GET_ALL_REMINDERS_ENDPOINT = "/reminder/all";

        @Test
        void shouldGetAllReminders_WithPagination() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_REMINDERS_ENDPOINT)
                    .param("page", "0").param("size", "10").with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1));
        }

        @Test
        void shouldReturnEmptyList_WhenNoReminders() throws Exception {
            User user = createTestUser();

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_REMINDERS_ENDPOINT)
                    .param("page", "0").param("size", "10").with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());
        }

        @Test
        void shouldReturnOnlyUserReminders() throws Exception {
            User user1 = createTestUser();
            Medicine medicine1 = createMedicineForUser(user1);

            Reminder reminder1 = Reminder.builder().patient((Patient) user1).medicine(medicine1)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder1);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient patient2 = Patient.builder().email("user2@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Jane").lastName("Doe")
                    .roles(Set.of(patientRole)).enabled(true).build();
            User user2 = patientRepository.save(patient2);

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_REMINDERS_ENDPOINT)
                    .param("page", "0").param("size", "10").with(withUserId(user2)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /reminder/{reminder-id} - Get Reminder By ID")
    class GetReminderById {
        private static final String GET_REMINDER_BY_ID_ENDPOINT = "/reminder/{reminder-id}";

        @Test
        void shouldGetReminderById_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_REMINDER_BY_ID_ENDPOINT, reminder.getId()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicineId")
                            .value(medicine.getId().toString()));
        }

        @Test
        void shouldReturnNotFound_WhenReminderDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.get(GET_REMINDER_BY_ID_ENDPOINT, nonExistentId)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnNotFound_WhenUserDoesNotOwnReminder() throws Exception {
            User owner = createTestUser();
            Medicine medicine = createMedicineForUser(owner);

            Reminder reminder = Reminder.builder().patient((Patient) owner).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient otherPatient = Patient.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).enabled(true).build();
            User otherUser = patientRepository.save(otherPatient);

            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_REMINDER_BY_ID_ENDPOINT, reminder.getId()).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /reminder/updateVibration/{vibrate}/reminder/{reminder-id} - Update Vibration")
    class UpdateVibration {
        private static final String UPDATE_VIBRATION_ENDPOINT = "/reminder/updateVibration/{vibrate}/reminder/{reminder-id}";

        @Test
        void shouldUpdateVibration_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            mockMvc.perform(
                    MockMvcRequestBuilders.patch(UPDATE_VIBRATION_ENDPOINT, false, reminder.getId())
                            .with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.vibrate").value(false));
        }

        @Test
        void shouldReturnNotFound_WhenReminderDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders
                    .patch(UPDATE_VIBRATION_ENDPOINT, true, nonExistentId).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnNotFound_WhenUserDoesNotOwnReminder() throws Exception {
            User owner = createTestUser();
            Medicine medicine = createMedicineForUser(owner);

            Reminder reminder = Reminder.builder().patient((Patient) owner).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient otherPatient = Patient.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).enabled(true).build();
            User otherUser = patientRepository.save(otherPatient);

            mockMvc.perform(
                    MockMvcRequestBuilders.patch(UPDATE_VIBRATION_ENDPOINT, false, reminder.getId())
                            .with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /reminder/{reminder-id} - Delete Reminder")
    class DeleteReminder {
        private static final String DELETE_REMINDER_ENDPOINT = "/reminder/{reminder-id}";

        @Test
        void shouldDeleteReminder_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_REMINDER_ENDPOINT, reminder.getId()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            assertThat(reminderRepository.findById(reminder.getId())).isEmpty();
        }

        @Test
        void shouldReturnNotFound_WhenReminderDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_REMINDER_ENDPOINT, nonExistentId)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnNotFound_WhenUserDoesNotOwnReminder() throws Exception {
            User owner = createTestUser();
            Medicine medicine = createMedicineForUser(owner);

            Reminder reminder = Reminder.builder().patient((Patient) owner).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient otherPatient = Patient.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).enabled(true).build();
            User otherUser = patientRepository.save(otherPatient);

            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_REMINDER_ENDPOINT, reminder.getId()).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /reminder/disable/{reminder-id}/{disable} - Disable Reminder")
    class DisableReminder {
        private static final String DISABLE_REMINDER_ENDPOINT = "/reminder/disable/{reminder-id}/{disable}";

        @Test
        void shouldDisableReminder_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).disable(false).build();
            reminderRepository.save(reminder);

            mockMvc.perform(
                    MockMvcRequestBuilders.patch(DISABLE_REMINDER_ENDPOINT, reminder.getId(), true)
                            .with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Reminder updatedReminder = reminderRepository.findById(reminder.getId()).orElseThrow();
            assertThat(updatedReminder.getDisable()).isTrue();
        }

        @Test
        void shouldEnableReminder_Successfully() throws Exception {
            User user = createTestUser();
            Medicine medicine = createMedicineForUser(user);

            Reminder reminder = Reminder.builder().patient((Patient) user).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).disable(true).build();
            reminderRepository.save(reminder);

            mockMvc.perform(
                    MockMvcRequestBuilders.patch(DISABLE_REMINDER_ENDPOINT, reminder.getId(), false)
                            .with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Reminder updatedReminder = reminderRepository.findById(reminder.getId()).orElseThrow();
            assertThat(updatedReminder.getDisable()).isFalse();
        }

        @Test
        void shouldReturnNotFound_WhenReminderDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders
                    .patch(DISABLE_REMINDER_ENDPOINT, nonExistentId, true).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnNotFound_WhenUserDoesNotOwnReminder() throws Exception {
            User owner = createTestUser();
            Medicine medicine = createMedicineForUser(owner);

            Reminder reminder = Reminder.builder().patient((Patient) owner).medicine(medicine)
                    .vibrate(true).status(ReminderStatus.UPCOMING)
                    .nextReminder(LocalDateTime.now().plusHours(1)).build();
            reminderRepository.save(reminder);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            // Create Patient directly (Patient extends User)
            Patient otherPatient = Patient.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).enabled(true).build();
            User otherUser = patientRepository.save(otherPatient);

            mockMvc.perform(
                    MockMvcRequestBuilders.patch(DISABLE_REMINDER_ENDPOINT, reminder.getId(), true)
                            .with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }
}
