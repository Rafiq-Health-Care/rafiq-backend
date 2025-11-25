package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.medicine.AddMedicineRequest;
import com.nexaworks.rafiq.dto.request.medicine.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicineRequest;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.entities.enums.Action;
import com.nexaworks.rafiq.entities.enums.Gender;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.MedicineType;
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
    @BeforeEach
    void setUp() {
        medicineRepository.deleteAll();
        patientRepository.deleteAll();
        drugRepository.deleteAll();
        userRepository.deleteAll();
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

        User user = User.builder().email(email).password(passwordEncoder.encode("Valid@1234"))
                .firstName(firstName).lastName(lastName).phone(phone).age(30).gender(gender)
                .roles(Set.of(patientRole)).enabled(true)
                .patientProfile(PatientProfile.builder().build()).build();
        return userRepository.save(user);
    }

    @Nested
    @DisplayName("Add Medicine")
    class AddMedicine {
        private final String ADD_MEDICINE_ENDPOINT = "/medicines/add";

        @Test
        @DisplayName("Should add medicine and return 200 ok and medicine when user doesn't exceed the limit, medicine wasn't added before and valid request")
        void shouldAddMedicine_WhenRequestIsValidAndUserDoesntExceedLimit() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            UUID medicineId = drugRepository.findAll().get(0).getId();
            AddMedicineRequest addMedicineRequest = new AddMedicineRequest(medicineId, "20 ml",
                    MedicineFrequency.AS_NEEDED, null, null, Instant.now(), null, null, null);
            String payload = objectMapper.writeValueAsString(addMedicineRequest);

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name")
                            .value(drug.getTradeName()));
        }
        @Test
        @DisplayName("Should return 400 Bad Request when medicine id is invalid")
        void shouldReturnBadRequestWhenMedicineIdIsInvalid() throws Exception {
            User user = createTestUser();
            AddMedicineRequest addMedicineRequest = new AddMedicineRequest(null, "20 ml",
                    MedicineFrequency.AS_NEEDED, null, null, Instant.now(), null, null, null);
            String payload = objectMapper.writeValueAsString(addMedicineRequest);
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
        @Test
        @DisplayName("Should return 409 Conflict when medicine is already added before")
        void shouldReturnConflictWhenMedicineIsAlreadyAddedBefore() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            UUID medicineId = drugRepository.findAll().get(0).getId();
            AddMedicineRequest addMedicineRequest = new AddMedicineRequest(medicineId, "20 ml",
                    MedicineFrequency.AS_NEEDED, null, null, Instant.now(), null, null, null);
            String payload = objectMapper.writeValueAsString(addMedicineRequest);

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isConflict());
        }
        @Test
        @DisplayName("Should return 422 Unprocessable Entity when user exceed the medicine limit")
        void shouldReturnUnprocessableEntityWhenUserExceedTheMedicineLimit() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();
            UUID medicineId = drugRepository.findAll().get(0).getId();
            AddMedicineRequest addMedicineRequest = new AddMedicineRequest(medicineId, "20 ml",
                    MedicineFrequency.AS_NEEDED, null, null, Instant.now(), null, null, null);
            String payload = objectMapper.writeValueAsString(addMedicineRequest);
            for (int i = 0; i < 200; i++) {
                Drug tempDrug = createDrug();
                medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED)
                        .dosage("100 mg").drug(tempDrug).patient(user.getPatientProfile()).build());
            }
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
        }

        private Drug createDrug() {
            Drug drug = Drug.builder().tradeName("panadol").dosageForm("tablet").build();
            return drugRepository.save(drug);
        }
    }

    @Nested
    @DisplayName("Get All Medicines")
    class GetAllMedicines {
        private final String GET_ALL_MEDICINES_ENDPOINT = "/medicines";

        @Test
        @DisplayName("Should return 200 OK with paginated medicines when user has medicines")
        void shouldReturnPaginatedMedicines_WhenUserHasMedicines() throws Exception {
            User user = createTestUser();
            Drug drug1 = createDrugWithName("Panadol");
            Drug drug2 = createDrugWithName("Aspirin");
            Drug drug3 = createDrugWithName("Ibuprofen");

            // Add medicines for the user
            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED)
                    .dosage("100 mg").drug(drug1).patient(user.getPatientProfile()).build());
            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.TWICE)
                    .dosage("200 mg").drug(drug2).patient(user.getPatientProfile()).build());
            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.ONCE)
                    .dosage("150 mg").drug(drug3).patient(user.getPatientProfile()).build());

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when user has no medicines")
        void shouldReturnEmptyList_WhenUserHasNoMedicines() throws Exception {
            User user = createTestUser();

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
        }

        @Test
        @DisplayName("Should return paginated results with correct page size")
        void shouldReturnPaginatedResults_WithCorrectPageSize() throws Exception {
            User user = createTestUser();

            // Add 15 medicines for the user
            for (int i = 0; i < 15; i++) {
                Drug drug = createDrugWithName("Medicine" + i);
                medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED)
                        .dosage((100 + i) + " mg").drug(drug).patient(user.getPatientProfile())
                        .build());
            }

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .param("page", "0").param("size", "10")
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(10))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(15))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(10))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(false));
        }

        @Test
        @DisplayName("Should return second page of results correctly")
        void shouldReturnSecondPage_WhenRequested() throws Exception {
            User user = createTestUser();

            for (int i = 0; i < 15; i++) {
                Drug drug = createDrugWithName("Medicine" + i);
                medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED)
                        .dosage((100 + i) + " mg").drug(drug).patient(user.getPatientProfile())
                        .build());
            }

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .param("page", "1").param("size", "10")
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(5))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(15))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(10))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
        }
        @Test
        @DisplayName("Should only return medicines for authenticated user")
        void shouldOnlyReturnMedicinesForAuthenticatedUser() throws Exception {
            User user1 = createTestUser();
            User user2 = createTestUser("another@test.com", "Jane", "Smith", "+12345678902",
                    Gender.FEMALE);
            Drug drug1 = createDrugWithName("Medicine1");
            Drug drug2 = createDrugWithName("Medicine2");

            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED)
                    .dosage("100 mg").drug(drug1).patient(user1.getPatientProfile()).build());

            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.TWICE)
                    .dosage("200 mg").drug(drug2).patient(user2.getPatientProfile()).build());

            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .with(SecurityMockMvcRequestPostProcessors.user(user1)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(1));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when user is not authenticated")
        void shouldReturnUnauthorized_WhenUserNotAuthenticated() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        private Drug createDrugWithName(String tradeName) {
            Drug drug = Drug.builder().tradeName(tradeName).dosageForm("tablet").build();
            return drugRepository.save(drug);
        }
    }

    @Nested
    @DisplayName("Get Medicine By ID")
    class GetMedicineById {
        private final String GET_MEDICINE_BY_ID_ENDPOINT = "/medicines/{id}";

        @Test
        @DisplayName("Should return 200 OK with medicine details when medicine exists and belongs to user")
        void shouldReturnMedicineDetails_WhenMedicineExistsAndBelongsToUser() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Aspirin");
            Medicine medicine = medicineRepository
                    .save(Medicine.builder().frequency(MedicineFrequency.TWICE).dosage("100 mg")
                            .drug(drug).name("Aspirin").patient(user.getPatientProfile()).build());

            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_MEDICINE_BY_ID_ENDPOINT, medicine.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.id")
                            .value(medicine.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.name").value("Aspirin"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.dosage").value("100 mg"))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.medicine.frequency").value("TWICE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reminders").isArray());
        }

        @Test
        @DisplayName("Should return medicine with reminders when medicine has reminders")
        void shouldReturnMedicineWithReminders_WhenMedicineHasReminders() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Paracetamol");
            Medicine medicine = medicineRepository.save(
                    Medicine.builder().frequency(MedicineFrequency.ONCE).dosage("500 mg").drug(drug)
                            .name("Paracetamol").patient(user.getPatientProfile()).build());

            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_MEDICINE_BY_ID_ENDPOINT, medicine.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.id")
                            .value(medicine.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reminders").isArray());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine does not exist")
        void shouldReturnNotFound_WhenMedicineDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.get(GET_MEDICINE_BY_ID_ENDPOINT, nonExistentId)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine belongs to another user")
        void shouldReturnNotFound_WhenMedicineBelongsToAnotherUser() throws Exception {
            User user1 = createTestUser();
            User user2 = createTestUser("getbyid@test.com", "Jane", "Doe", "+12345678907",
                    Gender.FEMALE);
            Drug drug = createDrugWithName("Ibuprofen");

            // Create medicine for user2
            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("200 mg").drug(drug)
                    .name("Ibuprofen").patient(user2.getPatientProfile()).build());

            // User1 tries to access user2's medicine
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_MEDICINE_BY_ID_ENDPOINT, medicine.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user1)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when user is not authenticated")
        void shouldReturnUnauthorized_WhenUserNotAuthenticated() throws Exception {
            UUID medicineId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.get(GET_MEDICINE_BY_ID_ENDPOINT, medicineId))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when medicine ID is invalid")
        void shouldReturnBadRequest_WhenMedicineIdIsInvalid() throws Exception {
            User user = createTestUser();

            mockMvc.perform(MockMvcRequestBuilders.get("/medicines/{id}", "invalid-uuid")
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Should return medicine with all fields populated correctly")
        void shouldReturnMedicineWithAllFieldsPopulated() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Metformin");
            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(30, java.time.temporal.ChronoUnit.DAYS);

            Medicine medicine = medicineRepository.save(
                    Medicine.builder().frequency(MedicineFrequency.THIRD_TIMES).dosage("850 mg")
                            .drug(drug).name("Metformin").startDate(startDate).endDate(endDate)
                            .notes("Take with food").patient(user.getPatientProfile()).build());

            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_MEDICINE_BY_ID_ENDPOINT, medicine.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.id")
                            .value(medicine.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.name").value("Metformin"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.dosage").value("850 mg"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.frequency")
                            .value("THIRD_TIMES"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.notes")
                            .value("Take with food"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.startDate").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.endDate").exists());
        }

        private Drug createDrugWithName(String tradeName) {
            Drug drug = Drug.builder().tradeName(tradeName).dosageForm("tablet").build();
            return drugRepository.save(drug);
        }
    }

    @Nested
    @DisplayName("Delete Medicine By ID")
    class DeleteMedicineById {
        private final String DELETE_MEDICINE_BY_ID_ENDPOINT = "/medicines/{id}";

        @Test
        @DisplayName("Should delete medicine and return 204 No Content when medicine exists and belongs to user")
        void shouldDeleteMedicine_WhenMedicineExistsAndBelongsToUser() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Aspirin");
            Medicine medicine = medicineRepository
                    .save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg")
                            .drug(drug).name("Aspirin").patient(user.getPatientProfile()).build());

            UUID medicineId = medicine.getId();

            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_MEDICINE_BY_ID_ENDPOINT, medicineId)
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            // Verify medicine was deleted
            assertThat(medicineRepository.findById(medicineId)).isEmpty();
        }

        @Test
        @DisplayName("Should delete medicine with all associated data")
        void shouldDeleteMedicineWithAssociatedData() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Metformin");
            Medicine medicine = medicineRepository
                    .save(Medicine.builder().frequency(MedicineFrequency.TWICE).dosage("500 mg")
                            .drug(drug).name("Metformin").notes("Take with meals")
                            .patient(user.getPatientProfile()).build());

            UUID medicineId = medicine.getId();

            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_MEDICINE_BY_ID_ENDPOINT, medicineId)
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            // Verify medicine was deleted from database
            assertThat(medicineRepository.findById(medicineId)).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine does not exist")
        void shouldReturnNotFound_WhenMedicineDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_MEDICINE_BY_ID_ENDPOINT, nonExistentId)
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine belongs to another user")
        void shouldReturnNotFound_WhenMedicineBelongsToAnotherUser() throws Exception {
            User user1 = createTestUser();
            User user2 = createTestUser("delete@test.com", "Jane", "User", "+12345678904",
                    Gender.FEMALE);
            Drug drug = createDrugWithName("Paracetamol");

            // Create medicine for user2
            Medicine medicine = medicineRepository.save(
                    Medicine.builder().frequency(MedicineFrequency.ONCE).dosage("650 mg").drug(drug)
                            .name("Paracetamol").patient(user2.getPatientProfile()).build());

            UUID medicineId = medicine.getId();

            // User1 tries to delete user2's medicine
            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_MEDICINE_BY_ID_ENDPOINT, medicineId)
                            .with(SecurityMockMvcRequestPostProcessors.user(user1)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

            // Verify medicine still exists
            assertThat(medicineRepository.findById(medicineId)).isPresent();
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when user is not authenticated")
        void shouldReturnUnauthorized_WhenUserNotAuthenticated() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Ibuprofen");
            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("400 mg").drug(drug)
                    .name("Ibuprofen").patient(user.getPatientProfile()).build());

            UUID medicineId = medicine.getId();

            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_MEDICINE_BY_ID_ENDPOINT, medicineId))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());

            // Verify medicine still exists
            assertThat(medicineRepository.findById(medicineId)).isPresent();
        }

        @Test
        @DisplayName("Should return 400 Bad Request when medicine ID is invalid")
        void shouldReturnBadRequest_WhenMedicineIdIsInvalid() throws Exception {
            User user = createTestUser();

            mockMvc.perform(MockMvcRequestBuilders.delete("/medicines/{id}", "invalid-uuid")
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Should not affect other user's medicines when deleting")
        void shouldNotAffectOtherUserMedicines_WhenDeleting() throws Exception {
            User user1 = createTestUser();
            User user2 = createTestUser("user2delete@test.com", "John", "Smith", "+12345678905",
                    Gender.MALE);
            Drug drug1 = createDrugWithName("Medicine1");
            Drug drug2 = createDrugWithName("Medicine2");

            // Create medicine for user1
            Medicine medicine1 = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.ONCE).dosage("100 mg").drug(drug1)
                    .name("Medicine1").patient(user1.getPatientProfile()).build());

            // Create medicine for user2
            Medicine medicine2 = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.TWICE).dosage("200 mg").drug(drug2)
                    .name("Medicine2").patient(user2.getPatientProfile()).build());

            // User1 deletes their medicine
            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_MEDICINE_BY_ID_ENDPOINT, medicine1.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user1)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            // Verify only user1's medicine was deleted
            assertThat(medicineRepository.findById(medicine1.getId())).isEmpty();
            assertThat(medicineRepository.findById(medicine2.getId())).isPresent();
        }
        private Drug createDrugWithName(String tradeName) {
            Drug drug = Drug.builder().tradeName(tradeName).dosageForm("tablet").build();
            return drugRepository.save(drug);
        }
    }

    @Nested
    @DisplayName("Update Medicine with PUT")
    class UpdateMedicineWithPut {
        private final String UPDATE_MEDICINE_ENDPOINT = "/medicines/{id}";

        @Test
        @DisplayName("Should update medicine and return 200 OK when all fields are valid")
        void shouldUpdateMedicine_WhenAllFieldsAreValid() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Aspirin");
            Instant originalStartDate = Instant.now();

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg").drug(drug)
                    .name("Aspirin").notes("Old notes").startDate(originalStartDate)
                    .patient(user.getPatientProfile()).build());

            Instant newStartDate = Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS);
            Instant newEndDate = newStartDate.plus(30, java.time.temporal.ChronoUnit.DAYS);

            UpdateMedicineRequest updateRequest = new UpdateMedicineRequest("Aspirin Updated",
                    "200 mg", "Take with food", MedicineFrequency.TWICE, newStartDate, newEndDate,
                    MedicineType.PRESCRIPTION, MedicineStatus.ACTIVE);

            String payload = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Medicine updated successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.id")
                            .value(medicine.getId().toString()))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.name").value("Aspirin Updated"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("200 mg"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.frequency").value("TWICE"))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.notes").value("Take with food"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.type").value("PRESCRIPTION"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("ACTIVE"));

            // Verify database was updated
            Medicine updatedMedicine = medicineRepository.findById(medicine.getId()).get();
            assertThat(updatedMedicine.getName()).isEqualTo("Aspirin Updated");
            assertThat(updatedMedicine.getDosage()).isEqualTo("200 mg");
            assertThat(updatedMedicine.getFrequency()).isEqualTo(MedicineFrequency.TWICE);
        }

        @Test
        @DisplayName("Should update all medicine fields correctly")
        void shouldUpdateAllMedicineFields() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Metformin");

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.ONCE).dosage("500 mg").drug(drug).name("Metformin")
                    .notes("Original notes").patient(user.getPatientProfile()).build());

            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(60, java.time.temporal.ChronoUnit.DAYS);

            UpdateMedicineRequest updateRequest = new UpdateMedicineRequest(
                    "Metformin Extended Release", "1000 mg", "Take with evening meal",
                    MedicineFrequency.ONCE, startDate, endDate, MedicineType.PRESCRIPTION,
                    MedicineStatus.ACTIVE);

            String payload = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name")
                            .value("Metformin Extended Release"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("1000 mg"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.notes")
                            .value("Take with evening meal"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when required fields are missing")
        void shouldReturnBadRequest_WhenRequiredFieldsAreMissing() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Paracetamol");

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("500 mg").drug(drug)
                    .name("Paracetamol").patient(user.getPatientProfile()).build());

            // Request with null name (required field)
            String invalidPayload = "{\"dosage\":\"600 mg\",\"frequency\":\"TWICE_DAILY\"}";

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(invalidPayload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine does not exist")
        void shouldReturnNotFound_WhenMedicineDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(30, java.time.temporal.ChronoUnit.DAYS);

            UpdateMedicineRequest updateRequest = new UpdateMedicineRequest("Medicine Name",
                    "100 mg", "Notes", MedicineFrequency.ONCE, startDate, endDate,
                    MedicineType.PRESCRIPTION, MedicineStatus.ACTIVE);

            String payload = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine belongs to another user")
        void shouldReturnNotFound_WhenMedicineBelongsToAnotherUser() throws Exception {
            User user1 = createTestUser();
            User user2 = createTestUser("updateuser@test.com", "Update", "User", "+12345678908",
                    Gender.FEMALE);
            Drug drug = createDrugWithName("Ibuprofen");

            // Create medicine for user2
            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("400 mg").drug(drug)
                    .name("Ibuprofen").patient(user2.getPatientProfile()).build());

            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(30, java.time.temporal.ChronoUnit.DAYS);

            UpdateMedicineRequest updateRequest = new UpdateMedicineRequest("Ibuprofen Updated",
                    "600 mg", "Updated notes", MedicineFrequency.TWICE, startDate, endDate,
                    MedicineType.PRESCRIPTION, MedicineStatus.ACTIVE);

            String payload = objectMapper.writeValueAsString(updateRequest);

            // User1 tries to update user2's medicine
            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user1)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

            // Verify medicine was not updated
            Medicine unchangedMedicine = medicineRepository.findById(medicine.getId()).get();
            assertThat(unchangedMedicine.getName()).isEqualTo("Ibuprofen");
            assertThat(unchangedMedicine.getDosage()).isEqualTo("400 mg");
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when user is not authenticated")
        void shouldReturnUnauthorized_WhenUserNotAuthenticated() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Amoxicillin");

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.THIRD_TIMES).dosage("500 mg").drug(drug)
                    .name("Amoxicillin").patient(user.getPatientProfile()).build());

            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(7, java.time.temporal.ChronoUnit.DAYS);

            UpdateMedicineRequest updateRequest = new UpdateMedicineRequest("Amoxicillin Updated",
                    "750 mg", "Notes", MedicineFrequency.TWICE, startDate, endDate,
                    MedicineType.PRESCRIPTION, MedicineStatus.ACTIVE);

            String payload = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when medicine ID is invalid")
        void shouldReturnBadRequest_WhenMedicineIdIsInvalid() throws Exception {
            User user = createTestUser();

            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(30, java.time.temporal.ChronoUnit.DAYS);

            UpdateMedicineRequest updateRequest = new UpdateMedicineRequest("Medicine", "100 mg",
                    "Notes", MedicineFrequency.ONCE, startDate, endDate, MedicineType.PRESCRIPTION,
                    MedicineStatus.ACTIVE);

            String payload = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(MockMvcRequestBuilders.put("/medicines/{id}", "invalid-uuid")
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Should update medicine status and type")
        void shouldUpdateMedicineStatusAndType() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Vitamin D");

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.ONCE).dosage("1000 IU").drug(drug)
                    .name("Vitamin D").type(MedicineType.TABLET).status(MedicineStatus.ACTIVE)
                    .patient(user.getPatientProfile()).build());

            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(90, java.time.temporal.ChronoUnit.DAYS);

            UpdateMedicineRequest updateRequest = new UpdateMedicineRequest("Vitamin D3", "2000 IU",
                    "Supplement", MedicineFrequency.ONCE, startDate, endDate, MedicineType.LIQUID,
                    MedicineStatus.INACTIVE);

            String payload = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.type").value("LIQUID"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("INACTIVE"));

            // Verify database was updated
            Medicine updatedMedicine = medicineRepository.findById(medicine.getId()).get();
            assertThat(updatedMedicine.getType()).isEqualTo(MedicineType.LIQUID);
            assertThat(updatedMedicine.getStatus()).isEqualTo(MedicineStatus.INACTIVE);
        }

        private Drug createDrugWithName(String tradeName) {
            Drug drug = Drug.builder().tradeName(tradeName).dosageForm("tablet").build();
            return drugRepository.save(drug);
        }
    }

    @Nested
    @DisplayName("Update Medicine with PATCH")
    class UpdateMedicineWithPatch {
        private final String PATCH_MEDICINE_ENDPOINT = "/medicines/{id}";

        @Test
        @DisplayName("Should partially update medicine fields when valid optional fields are provided")
        void shouldPartiallyUpdateMedicine_WhenValidOptionalFieldsProvided() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Aspirin");

            Medicine medicine = medicineRepository
                    .save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg")
                            .drug(drug).name("Aspirin").notes("Original notes")
                            .patient(user.getPatientProfile()).build());

            // Patch only dosage and notes
            String patchPayload = "{\"dosage\":\"200 mg\",\"notes\":\"Updated notes\"}";

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(patchPayload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Medicine updated successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("200 mg"))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.notes").value("Updated notes"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Aspirin"));

            // Verify unchanged fields remain the same
            Medicine updatedMedicine = medicineRepository.findById(medicine.getId()).get();
            assertThat(updatedMedicine.getName()).isEqualTo("Aspirin");
            assertThat(updatedMedicine.getDosage()).isEqualTo("200 mg");
            assertThat(updatedMedicine.getNotes()).isEqualTo("Updated notes");
            assertThat(updatedMedicine.getFrequency()).isEqualTo(MedicineFrequency.AS_NEEDED);
        }

        @Test
        @DisplayName("Should update only specified fields and leave others unchanged")
        void shouldUpdateOnlySpecifiedFields_LeavingOthersUnchanged() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Metformin");

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.TWICE).dosage("500 mg").drug(drug)
                    .name("Metformin").notes("Take with meals").type(MedicineType.TABLET)
                    .status(MedicineStatus.ACTIVE).patient(user.getPatientProfile()).build());

            // Update only frequency
            String patchPayload = "{\"frequency\":\"THIRD_TIMES\"}";

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(patchPayload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.frequency").value("THIRD_TIMES"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("500 mg"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.notes")
                            .value("Take with meals"));

            // Verify only frequency changed
            Medicine updatedMedicine = medicineRepository.findById(medicine.getId()).get();
            assertThat(updatedMedicine.getFrequency()).isEqualTo(MedicineFrequency.THIRD_TIMES);
            assertThat(updatedMedicine.getDosage()).isEqualTo("500 mg");
            assertThat(updatedMedicine.getNotes()).isEqualTo("Take with meals");
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine does not exist")
        void shouldReturnNotFound_WhenMedicineDoesNotExist() throws Exception {
            User user = createTestUser();
            UUID nonExistentId = UUID.randomUUID();

            String patchPayload = "{\"dosage\":\"100 mg\"}";

            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON).content(patchPayload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine belongs to another user")
        void shouldReturnNotFound_WhenMedicineBelongsToAnotherUser() throws Exception {
            User user1 = createTestUser();
            User user2 = createTestUser("patch@test.com", "Patch", "User", "+12345678909",
                    Gender.FEMALE);
            Drug drug = createDrugWithName("Paracetamol");

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("500 mg").drug(drug)
                    .name("Paracetamol").patient(user2.getPatientProfile()).build());

            String patchPayload = "{\"dosage\":\"650 mg\"}";

            // User1 tries to patch user2's medicine
            mockMvc.perform(MockMvcRequestBuilders.patch(PATCH_MEDICINE_ENDPOINT, medicine.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(patchPayload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user1)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

            // Verify medicine was not updated
            Medicine unchangedMedicine = medicineRepository.findById(medicine.getId()).get();
            assertThat(unchangedMedicine.getDosage()).isEqualTo("500 mg");
        }

        private Drug createDrugWithName(String tradeName) {
            Drug drug = Drug.builder().tradeName(tradeName).dosageForm("tablet").build();
            return drugRepository.save(drug);
        }
    }

    @Nested
    @DisplayName("Bulk Medicine Operations")
    class BulkMedicineOperations {
        private final String BULK_MEDICINE_ENDPOINT = "/medicines/bulk";

        @Test
        @DisplayName("Should delete multiple medicines successfully")
        void shouldDeleteMultipleMedicines_Successfully() throws Exception {
            User user = createTestUser();
            Drug drug1 = createDrugWithName("Medicine1");
            Drug drug2 = createDrugWithName("Medicine2");
            Drug drug3 = createDrugWithName("Medicine3");

            Medicine medicine1 = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg").drug(drug1)
                    .name("Medicine1").patient(user.getPatientProfile()).build());
            Medicine medicine2 = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.ONCE).dosage("200 mg").drug(drug2)
                    .name("Medicine2").patient(user.getPatientProfile()).build());
            Medicine medicine3 = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.TWICE).dosage("300 mg").drug(drug3)
                    .name("Medicine3").patient(user.getPatientProfile()).build());

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine1.getId(), medicine2.getId(), medicine3.getId()), Action.DELETE,
                    Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Bulk operation completed successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds").isEmpty());

            // Verify all medicines were deleted
            assertThat(medicineRepository.findById(medicine1.getId())).isEmpty();
            assertThat(medicineRepository.findById(medicine2.getId())).isEmpty();
            assertThat(medicineRepository.findById(medicine3.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should mark multiple medicines as active")
        void shouldMarkMultipleMedicinesAsActive() throws Exception {
            User user = createTestUser();
            Drug drug1 = createDrugWithName("Drug1");
            Drug drug2 = createDrugWithName("Drug2");

            Medicine medicine1 = medicineRepository
                    .save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg")
                            .drug(drug1).name("Drug1").status(MedicineStatus.INACTIVE)
                            .patient(user.getPatientProfile()).build());
            Medicine medicine2 = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.ONCE).dosage("200 mg").drug(drug2).name("Drug2")
                    .status(MedicineStatus.INACTIVE).patient(user.getPatientProfile()).build());

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine1.getId(), medicine2.getId()), Action.MARK_ACTIVE,
                    Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds").isEmpty());

            // Verify status changed to ACTIVE
            Medicine updated1 = medicineRepository.findById(medicine1.getId()).get();
            Medicine updated2 = medicineRepository.findById(medicine2.getId()).get();
            assertThat(updated1.getStatus()).isEqualTo(MedicineStatus.ACTIVE);
            assertThat(updated2.getStatus()).isEqualTo(MedicineStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should mark multiple medicines as inactive")
        void shouldMarkMultipleMedicinesAsInactive() throws Exception {
            User user = createTestUser();
            Drug drug1 = createDrugWithName("ActiveDrug1");
            Drug drug2 = createDrugWithName("ActiveDrug2");

            Medicine medicine1 = medicineRepository
                    .save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg")
                            .drug(drug1).name("ActiveDrug1").status(MedicineStatus.ACTIVE)
                            .patient(user.getPatientProfile()).build());
            Medicine medicine2 = medicineRepository
                    .save(Medicine.builder().frequency(MedicineFrequency.TWICE).dosage("200 mg")
                            .drug(drug2).name("ActiveDrug2").status(MedicineStatus.ACTIVE)
                            .patient(user.getPatientProfile()).build());

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine1.getId(), medicine2.getId()), Action.MARK_INACTIVE,
                    Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds").isEmpty());

            // Verify status changed to INACTIVE
            Medicine updated1 = medicineRepository.findById(medicine1.getId()).get();
            Medicine updated2 = medicineRepository.findById(medicine2.getId()).get();
            assertThat(updated1.getStatus()).isEqualTo(MedicineStatus.INACTIVE);
            assertThat(updated2.getStatus()).isEqualTo(MedicineStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should handle partial failures and return failed IDs")
        void shouldHandlePartialFailures_ReturningFailedIds() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("ValidDrug");

            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg").drug(drug)
                    .name("ValidDrug").patient(user.getPatientProfile()).build());

            UUID nonExistentId = UUID.randomUUID();

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(medicine.getId(), nonExistentId), Action.DELETE, Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds.length()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds[0]")
                            .value(nonExistentId.toString()));

            // Verify valid medicine was deleted
            assertThat(medicineRepository.findById(medicine.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should not affect medicines belonging to other users")
        void shouldNotAffectOtherUserMedicines() throws Exception {
            User user1 = createTestUser();
            User user2 = createTestUser("bulk@test.com", "Bulk", "User", "+12345678910",
                    Gender.MALE);
            Drug drug1 = createDrugWithName("User1Drug");
            Drug drug2 = createDrugWithName("User2Drug");

            Medicine user1Medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.AS_NEEDED).dosage("100 mg").drug(drug1)
                    .name("User1Drug").patient(user1.getPatientProfile()).build());

            Medicine user2Medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.ONCE).dosage("200 mg").drug(drug2)
                    .name("User2Drug").patient(user2.getPatientProfile()).build());

            // User1 tries to delete both medicines including user2's medicine
            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(user1Medicine.getId(), user2Medicine.getId()), Action.DELETE,
                    Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user1)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.failedIds.length()").value(1));

            // Verify only user1's medicine was deleted
            assertThat(medicineRepository.findById(user1Medicine.getId())).isEmpty();
            assertThat(medicineRepository.findById(user2Medicine.getId())).isPresent();
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when user is not authenticated")
        void shouldReturnUnauthorized_WhenUserNotAuthenticated() throws Exception {
            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(
                    List.of(UUID.randomUUID()), Action.DELETE, Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when medicine IDs list is empty")
        void shouldReturnBadRequest_WhenMedicineIdsListIsEmpty() throws Exception {
            User user = createTestUser();

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(List.of(),
                    Action.DELETE, Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle large batch operations efficiently")
        void shouldHandleLargeBatchOperations_Efficiently() throws Exception {
            User user = createTestUser();
            List<UUID> medicineIds = new java.util.ArrayList<>();

            // Create 10 medicines
            for (int i = 0; i < 10; i++) {
                Drug drug = createDrugWithName("BatchMedicine" + i);
                Medicine medicine = medicineRepository.save(Medicine.builder()
                        .frequency(MedicineFrequency.AS_NEEDED).dosage((100 + i * 10) + " mg")
                        .drug(drug).name("BatchMedicine" + i).status(MedicineStatus.INACTIVE)
                        .patient(user.getPatientProfile()).build());
                medicineIds.add(medicine.getId());
            }

            BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                    Action.MARK_ACTIVE, Optional.empty());
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post(BULK_MEDICINE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.successCount").value(10))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.failedIds").isEmpty());

            // Verify all medicines were updated
            long activeCount = medicineRepository.findAll().stream()
                    .filter(m -> m.getStatus() == MedicineStatus.ACTIVE).count();
            assertThat(activeCount).isEqualTo(10L);
        }

        private Drug createDrugWithName(String tradeName) {
            Drug drug = Drug.builder().tradeName(tradeName).dosageForm("tablet").build();
            return drugRepository.save(drug);
        }
    }

}
