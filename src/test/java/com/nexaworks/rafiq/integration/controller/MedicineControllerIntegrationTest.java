package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

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
import com.nexaworks.rafiq.dto.request.medicine.AddMedicineRequest;
import com.nexaworks.rafiq.dto.request.medicine.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicinePatchRequest;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicineRequest;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.entities.enums.Action;
import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.Gender;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.MedicineType;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.*;

@DisplayName("Medicine Controller Integration Test Cases")
public class MedicineControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    MockMvc mockMvc;
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
    GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        medicineRepository.deleteAll();
        patientRepository.deleteAll();
        drugRepository.deleteAll();
        userRepository.deleteAll();
        groupRepository.deleteAll();

    }

    private User createTestUser() {
        return createTestUser("email@test.com", "John", "Doe", "+12345678901", Gender.MALE);
    }

    private User createTestUser(String email, String firstName, String lastName, String phone,
            Gender gender) {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        PatientProfile patientProfile = PatientProfile.builder().build();
        User user = User.builder().email(email).password(passwordEncoder.encode("Valid@1234"))
                .firstName(firstName).lastName(lastName).phone(phone)
                .birthDate(LocalDate.of(1990, 1, 1)).gender(gender).roles(Set.of(patientRole))
                .enabled(true).patientProfile(patientProfile).build();
        patientProfile.setUser(user);
        return userRepository.save(user);
    }

    private Drug createDrug() {
        Drug drug = Drug.builder().tradeName("Aspirin").drugGroup("NSAIDs").dosageForm("Tablet")
                .route("Oral").price(10.0).build();
        return drugRepository.save(drug);
    }

    @Nested
    @DisplayName("Add Medicine")
    class AddMedicine {
        private final String ADD_MEDICINE_ENDPOINT = "/medicines/add";

        @Test
        @DisplayName("Should add medicine successfully when all required fields are valid")
        void shouldAddMedicine_WhenAllRequiredFieldsAreValid() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Instant startDate = Instant.now();
            Instant endDate = startDate.plusSeconds(86400 * 30); // 30 days

            AddMedicineRequest request = new AddMedicineRequest(drug.getId(), "100mg",
                    MedicineFrequency.TWICE, ReminderFrequency.DAILY, List.of(), startDate, endDate,
                    "Take with food", MedicineType.PRESCRIPTION);

            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.patientId")
                            .value(user.getPatientProfile().getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Aspirin"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("100mg"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.frequency").value("TWICE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.reminderFrequency")
                            .value("DAILY"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.type").value("PRESCRIPTION"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.notes").value("Take with food"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.updatedAt").exists());

            assertThat(medicineRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return 409 Conflict when medicine already exists for patient")
        void shouldReturnConflict_WhenMedicineAlreadyExists() throws Exception {

            User user = createTestUser();
            Drug drug = createDrug();

            AddMedicineRequest request = new AddMedicineRequest(drug.getId(), "100mg",
                    MedicineFrequency.ONCE, ReminderFrequency.DAILY, null, Instant.now(), null,
                    null, null);

            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isCreated());

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isConflict());
        }

        @Test
        @DisplayName("Should return 422 Unprocessable Entity when user exceeds medicine limit")
        void shouldReturnUnprocessableEntity_WhenUserExceedsMedicineLimit() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();

            AddMedicineRequest request = new AddMedicineRequest(drug.getId(), "100mg",
                    MedicineFrequency.ONCE, ReminderFrequency.DAILY, null, Instant.now(), null,
                    null, null);

            String payload = objectMapper.writeValueAsString(request);

            for (int i = 0; i < 200; i++) {
                Drug tempDrug = Drug.builder().tradeName("Drug" + i).drugGroup("Group" + i)
                        .price(5.0).build();
                drugRepository.save(tempDrug);

                Medicine medicine = Medicine.builder().patient(user.getPatientProfile())
                        .drug(tempDrug).name(tempDrug.getTradeName()).dosage("100mg")
                        .frequency(MedicineFrequency.ONCE).status(MedicineStatus.ACTIVE)
                        .startDate(Instant.now()).build();
                medicineRepository.save(medicine);
            }

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when drug ID is invalid")
        void shouldReturnBadRequest_WhenDrugIdIsInvalid() throws Exception {
            User user = createTestUser();
            UUID invalidDrugId = UUID.randomUUID();

            AddMedicineRequest request = new AddMedicineRequest(invalidDrugId, "100mg",
                    MedicineFrequency.ONCE, null, null, Instant.now(), null, null, null);

            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Should add medicine with custom days when reminder frequency is CUSTOM")
        void shouldAddMedicine_WithCustomDaysWhenReminderFrequencyIsCustom() throws Exception {

            User user = createTestUser();
            Drug drug = createDrug();
            Instant startDate = Instant.now();

            AddMedicineRequest request = new AddMedicineRequest(drug.getId(), "500mg",
                    MedicineFrequency.THIRD_TIMES, ReminderFrequency.CUSTOM,
                    List.of(Day.MONDAY, Day.WEDNESDAY, Day.FRIDAY), startDate,
                    startDate.plusSeconds(86400 * 60), "Take after meals", MedicineType.TABLET);

            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.reminderFrequency")
                            .value("CUSTOM"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.customDays").isArray())
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.customDays.length()").value(3))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.frequency").value("THIRD_TIMES"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.type").value("TABLET"));

            Medicine savedMedicine = medicineRepository.findAll().get(0);
            assertThat(savedMedicine.getCustomDays()).isNotNull();
            assertThat(savedMedicine.getCustomDays().size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Get All Medicines")
    class GetAllMedicines {
        private final String GET_ALL_MEDICINES_ENDPOINT = "/medicines";

        @Test
        @DisplayName("Should return paginated medicines when user has medicines")
        void shouldReturnPaginatedMedicines_WhenUserHasMedicines() throws Exception {
            // Arrange
            User user = createTestUser();
            Drug drug1 = createDrug();
            Drug drug2 = Drug.builder().tradeName("Ibuprofen").drugGroup("NSAIDs").price(8.0)
                    .build();
            drugRepository.save(drug2);

            Medicine medicine1 = Medicine.builder().patient(user.getPatientProfile()).drug(drug1)
                    .name(drug1.getTradeName()).dosage("100mg").frequency(MedicineFrequency.TWICE)
                    .status(MedicineStatus.ACTIVE).startDate(Instant.now()).build();
            medicineRepository.save(medicine1);

            Medicine medicine2 = Medicine.builder().patient(user.getPatientProfile()).drug(drug2)
                    .name(drug2.getTradeName()).dosage("200mg")
                    .frequency(MedicineFrequency.THIRD_TIMES).status(MedicineStatus.ACTIVE)
                    .startDate(Instant.now()).build();
            medicineRepository.save(medicine2);

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("Should return empty list when user has no medicines")
        void shouldReturnEmptyList_WhenUserHasNoMedicines() throws Exception {
            // Arrange
            User user = createTestUser();

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(0));
        }

        @Test
        @DisplayName("Should return only authenticated user's medicines")
        void shouldReturnOnlyAuthenticatedUserMedicines() throws Exception {
            // Arrange
            User user1 = createTestUser();
            User user2 = createTestUser("other@test.com", "Jane", "Smith", "+19876543210",
                    Gender.FEMALE);
            Drug drug = createDrug();

            // Add medicine for user1
            Medicine medicine1 = Medicine.builder().patient(user1.getPatientProfile()).drug(drug)
                    .name(drug.getTradeName()).dosage("100mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).startDate(Instant.now()).build();
            medicineRepository.save(medicine1);

            // Add medicine for user2
            Medicine medicine2 = Medicine.builder().patient(user2.getPatientProfile()).drug(drug)
                    .name(drug.getTradeName()).dosage("200mg").frequency(MedicineFrequency.TWICE)
                    .status(MedicineStatus.ACTIVE).startDate(Instant.now()).build();
            medicineRepository.save(medicine2);

            // Act & Assert - user1 should only see their own medicine
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT).with(withUserId(user1)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.content[0].dosage").value("100mg"));
        }

        @Test
        @DisplayName("Should support pagination with custom page size")
        void shouldSupportPagination_WithCustomPageSize() throws Exception {
            // Arrange
            User user = createTestUser();
            Drug drug = createDrug();

            // Create 5 medicines
            for (int i = 0; i < 5; i++) {
                Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                        .name(drug.getTradeName()).dosage((i + 1) * 100 + "mg")
                        .frequency(MedicineFrequency.ONCE).status(MedicineStatus.ACTIVE)
                        .startDate(Instant.now()).build();
                medicineRepository.save(medicine);
            }

            // Act & Assert - Request page 0 with size 2
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .param("page", "0").param("size", "2").with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(5))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(2));
        }

        @Test
        @DisplayName("Should filter medicines by status when status filter is provided")
        void shouldFilterMedicinesByStatus_WhenStatusFilterProvided() throws Exception {
            // Arrange
            User user = createTestUser();
            Drug drug = createDrug();

            Medicine activeMedicine = Medicine.builder().patient(user.getPatientProfile())
                    .drug(drug).name(drug.getTradeName()).dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).status(MedicineStatus.ACTIVE)
                    .startDate(Instant.now()).build();
            medicineRepository.save(activeMedicine);

            Medicine inactiveMedicine = Medicine.builder().patient(user.getPatientProfile())
                    .drug(drug).name(drug.getTradeName()).dosage("200mg")
                    .frequency(MedicineFrequency.TWICE).status(MedicineStatus.INACTIVE)
                    .startDate(Instant.now()).build();
            medicineRepository.save(inactiveMedicine);

            // Act & Assert - Filter by ACTIVE status
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .param("status", "ACTIVE").with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.content[0].status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("GET /medicines/{id} - Get Medicine By ID")
    class GetMedicineById {
        private static final String GET_MEDICINE_BY_ID_ENDPOINT = "/medicines/{id}";

        @Test
        void shouldReturnMedicine_WhenMedicineExists() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Test Medicine").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_MEDICINE_BY_ID_ENDPOINT, medicine.getId()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.patientId")
                            .value(user.getPatientProfile().getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Medicine"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.dosage").value("10mg"));
        }

        @Test
        void shouldReturnNotFound_WhenMedicineDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.get(GET_MEDICINE_BY_ID_ENDPOINT, nonExistentId)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnForbidden_WhenAccessingOtherUserMedicine() throws Exception {
            User owner = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(owner.getPatientProfile()).drug(drug)
                    .name("Owner Medicine").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            PatientProfile otherPatient = PatientProfile.builder().build();
            User otherUser = User.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).enabled(true).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).patientProfile(otherPatient)
                    .build();
            otherPatient.setUser(otherUser);
            userRepository.save(otherUser);

            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_MEDICINE_BY_ID_ENDPOINT, medicine.getId()).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnMedicineWithGroup_WhenMedicineHasGroup() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Group group = Group.builder().name("Morning Pills").patient(user.getPatientProfile())
                    .build();
            groupRepository.save(group);

            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Test Medicine").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION).group(group)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_MEDICINE_BY_ID_ENDPOINT, medicine.getId()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.groupId")
                            .value(group.getId().toString()))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.groupName").value("Morning Pills"));
        }
    }

    @Nested
    @DisplayName("DELETE /medicines/{id} - Delete Medicine")
    class DeleteMedicine {
        private static final String DELETE_MEDICINE_ENDPOINT = "/medicines/{id}";

        @Test
        void shouldDeleteMedicine_WhenMedicineExists() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Test Medicine").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_MEDICINE_ENDPOINT, medicine.getId()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            assertThat(medicineRepository.findById(medicine.getId())).isEmpty();
        }

        @Test
        void shouldReturnNotFound_WhenDeletingNonExistentMedicine() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_MEDICINE_ENDPOINT, nonExistentId)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnForbidden_WhenDeletingOtherUserMedicine() throws Exception {
            User owner = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(owner.getPatientProfile()).drug(drug)
                    .name("Owner Medicine").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            PatientProfile otherPatient = PatientProfile.builder().build();
            User otherUser = User.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).enabled(true).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).patientProfile(otherPatient)
                    .build();
            otherPatient.setUser(otherUser);
            userRepository.save(otherUser);

            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_MEDICINE_ENDPOINT, medicine.getId()).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldDeleteMedicineAndReminders_WhenMedicineHasReminders() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Test Medicine").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_MEDICINE_ENDPOINT, medicine.getId()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            assertThat(medicineRepository.findById(medicine.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("PUT /medicines/{id} - Update Medicine")
    class UpdateMedicine {
        private static final String UPDATE_MEDICINE_ENDPOINT = "/medicines/{id}";

        @Test
        void shouldUpdateMedicine_WhenAllFieldsAreValid() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Old Name").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            UpdateMedicineRequest request = new UpdateMedicineRequest("New Name", "10mg",
                    "New notes", MedicineFrequency.TWICE, Instant.now(),
                    Instant.now().plusSeconds(86400 * 60), MedicineType.SUPPLEMENT,
                    MedicineStatus.INACTIVE, ReminderFrequency.DAILY, List.of());

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("New Name"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("10mg"));
        }

        @Test
        void shouldReturnNotFound_WhenUpdatingNonExistentMedicine() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            UpdateMedicineRequest request = new UpdateMedicineRequest("New Name", "10mg",
                    "New notes", MedicineFrequency.ONCE, Instant.now(),
                    Instant.now().plusSeconds(86400 * 30), MedicineType.PRESCRIPTION,
                    MedicineStatus.ACTIVE, ReminderFrequency.DAILY, List.of());

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnForbidden_WhenUpdatingOtherUserMedicine() throws Exception {
            User owner = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(owner.getPatientProfile()).drug(drug)
                    .name("Owner Medicine").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            PatientProfile otherPatient = PatientProfile.builder().build();
            User otherUser = User.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).enabled(true).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).patientProfile(otherPatient)
                    .build();
            otherPatient.setUser(otherUser);
            userRepository.save(otherUser);

            UpdateMedicineRequest request = new UpdateMedicineRequest("New Name", "10mg",
                    "New notes", MedicineFrequency.ONCE, Instant.now(),
                    Instant.now().plusSeconds(86400 * 30), MedicineType.PRESCRIPTION,
                    MedicineStatus.ACTIVE, ReminderFrequency.DAILY, List.of());

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnBadRequest_WhenRequestBodyIsInvalid() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Test Medicine").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content("{}").with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void shouldUpdateMedicine_WithCustomDaysWhenReminderFrequencyIsCustom() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Test Medicine").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            UpdateMedicineRequest request = new UpdateMedicineRequest("Updated Name", "15mg",
                    "Updated notes", MedicineFrequency.ONCE, Instant.now(),
                    Instant.now().plusSeconds(86400 * 60), MedicineType.PRESCRIPTION,
                    MedicineStatus.ACTIVE, ReminderFrequency.CUSTOM,
                    List.of(Day.MONDAY, Day.WEDNESDAY, Day.FRIDAY));

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true)).andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.customDays.length()").value(3));
        }
    }

    @Nested
    @DisplayName("PATCH /medicines/{id} - Partial Update Medicine")
    class PatchMedicine {
        private static final String PATCH_MEDICINE_ENDPOINT = "/medicines/{id}";

        @Test
        void shouldPartiallyUpdateMedicine_WhenUpdatingOnlyName() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Old Name").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            UpdateMedicinePatchRequest request = new UpdateMedicinePatchRequest(
                    Optional.of("New Name"), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("New Name"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("5mg"));
        }

        @Test
        void shouldPartiallyUpdateMedicine_WhenUpdatingMultipleFields() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Old Name").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            UpdateMedicinePatchRequest request = new UpdateMedicinePatchRequest(
                    Optional.of("New Name"), Optional.of("10mg"), Optional.empty(),
                    Optional.of(MedicineFrequency.TWICE), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("New Name"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("10mg"));
        }

        @Test
        void shouldReturnNotFound_WhenPatchingNonExistentMedicine() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            UpdateMedicinePatchRequest request = new UpdateMedicinePatchRequest(
                    Optional.of("New Name"), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldReturnForbidden_WhenPatchingOtherUserMedicine() throws Exception {
            User owner = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(owner.getPatientProfile()).drug(drug)
                    .name("Owner Medicine").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            PatientProfile otherPatient = PatientProfile.builder().build();
            User otherUser = User.builder().email("other@example.com")
                    .password(passwordEncoder.encode("password")).enabled(true).firstName("Other")
                    .lastName("User").roles(Set.of(patientRole)).patientProfile(otherPatient)
                    .build();
            otherPatient.setUser(otherUser);
            userRepository.save(otherUser);

            UpdateMedicinePatchRequest request = new UpdateMedicinePatchRequest(
                    Optional.of("New Name"), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(otherUser)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldPartiallyUpdateStatus_WhenUpdatingOnlyStatus() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            Medicine medicine = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Test Medicine").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();
            medicineRepository.save(medicine);

            UpdateMedicinePatchRequest request = new UpdateMedicinePatchRequest(Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.of(MedicineStatus.INACTIVE),
                    Optional.empty(), Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /medicines/bulk - Bulk Medicine Operations")
    class BulkMedicineOperations {
        private static final String BULK_MEDICINE_ENDPOINT = "/medicines/bulk";

        @Test
        void shouldDeleteMultipleMedicines_WhenActionIsDelete() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();

            Medicine medicine1 = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Medicine 1").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();

            Medicine medicine2 = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Medicine 2").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();

            medicineRepository.saveAll(List.of(medicine1, medicine2));

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine1.getId(), medicine2.getId()), Action.DELETE, Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(2))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.failedIds.length()").value(0));
        }

        @Test
        void shouldMarkMultipleMedicinesAsActive_WhenActionIsMarkActive() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();

            Medicine medicine1 = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Medicine 1").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.INACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();

            Medicine medicine2 = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Medicine 2").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.INACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();

            medicineRepository.saveAll(List.of(medicine1, medicine2));

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine1.getId(), medicine2.getId()), Action.MARK_ACTIVE,
                    Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(2));
        }

        @Test
        void shouldMoveMultipleMedicinesToGroup_WhenActionIsMoveToGroup() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();

            Group targetGroup = Group.builder().name("Evening Pills")
                    .patient(user.getPatientProfile()).build();
            groupRepository.save(targetGroup);

            Medicine medicine1 = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Medicine 1").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();

            Medicine medicine2 = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Medicine 2").dosage("10mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();

            medicineRepository.saveAll(List.of(medicine1, medicine2));

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine1.getId(), medicine2.getId()), Action.MOVE_TO_GROUP,
                    Optional.of(targetGroup.getId()));

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(2));
        }

        @Test
        void shouldReturnPartialSuccess_WhenSomeMedicinesFail() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();

            Medicine medicine1 = Medicine.builder().patient(user.getPatientProfile()).drug(drug)
                    .name("Medicine 1").dosage("5mg").frequency(MedicineFrequency.ONCE)
                    .status(MedicineStatus.ACTIVE).type(MedicineType.PRESCRIPTION)
                    .startDate(Instant.now()).endDate(Instant.now().plusSeconds(86400 * 30))
                    .build();

            medicineRepository.save(medicine1);
            UUID nonExistentId = UUID.randomUUID();

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine1.getId(), nonExistentId), Action.DELETE, Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds.length()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds[0]")
                            .value(nonExistentId.toString()));
        }

        @Test
        void shouldReturnBadRequest_WhenMedicineIdsListIsEmpty() throws Exception {
            User user = createTestUser();

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(List.of(),
                    Action.DELETE, Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }
}
