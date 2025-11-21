package com.nexaworks.rafiq.integration.controller;

import java.time.Instant;
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
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.enums.Gender;
import com.nexaworks.rafiq.enums.MedicineFrequency;
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
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        User user = User.builder().email("email@test.com")
                .password(passwordEncoder.encode("Valid@1234")).firstName("John").lastName("Doe")
                .phone("+12345678901").age(30).gender(Gender.MALE).roles(Set.of(patientRole))
                .enabled(true).patientProfile(PatientProfile.builder().build()).build();
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
                    MedicineFrequency.AS_NEEDED, Instant.now(), null, null, null);
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
                    MedicineFrequency.AS_NEEDED, Instant.now(), null, null, null);
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
                    MedicineFrequency.AS_NEEDED, Instant.now(), null, null, null);
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
                    MedicineFrequency.AS_NEEDED, Instant.now(), null, null, null);
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
            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.TWICE_DAILY)
                    .dosage("200 mg").drug(drug2).patient(user.getPatientProfile()).build());
            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.ONCE_DAILY)
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
            User user2 = createAnotherUser();
            Drug drug1 = createDrugWithName("Medicine1");
            Drug drug2 = createDrugWithName("Medicine2");

            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.AS_NEEDED)
                    .dosage("100 mg").drug(drug1).patient(user1.getPatientProfile()).build());

            medicineRepository.save(Medicine.builder().frequency(MedicineFrequency.TWICE_DAILY)
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

        private User createAnotherUser() {
            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            if (patientRole == null) {
                patientRole = new Role();
                patientRole.setName("ROLE_PATIENT");
                patientRole = roleRepository.save(patientRole);
            }

            User user = User.builder().email("another@test.com")
                    .password(passwordEncoder.encode("Valid@1234")).firstName("Jane")
                    .lastName("Smith").phone("+12345678902").age(25).gender(Gender.FEMALE)
                    .roles(Set.of(patientRole)).enabled(true)
                    .patientProfile(PatientProfile.builder().build()).build();
            return userRepository.save(user);
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
            Medicine medicine = medicineRepository.save(
                    Medicine.builder().frequency(MedicineFrequency.TWICE_DAILY).dosage("100 mg")
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
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.frequency")
                            .value("TWICE_DAILY"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reminders").isArray());
        }

        @Test
        @DisplayName("Should return medicine with reminders when medicine has reminders")
        void shouldReturnMedicineWithReminders_WhenMedicineHasReminders() throws Exception {
            User user = createTestUser();
            Drug drug = createDrugWithName("Paracetamol");
            Medicine medicine = medicineRepository.save(Medicine.builder()
                    .frequency(MedicineFrequency.ONCE_DAILY).dosage("500 mg").drug(drug)
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
            User user2 = createAnotherUser();
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
                    Medicine.builder().frequency(MedicineFrequency.THRICE_DAILY).dosage("850 mg")
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
                            .value("THRICE_DAILY"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.notes")
                            .value("Take with food"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.startDate").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicine.endDate").exists());
        }

        private Drug createDrugWithName(String tradeName) {
            Drug drug = Drug.builder().tradeName(tradeName).dosageForm("tablet").build();
            return drugRepository.save(drug);
        }

        private User createAnotherUser() {
            Role patientRole = roleRepository.findByName("ROLE_PATIENT");
            if (patientRole == null) {
                patientRole = new Role();
                patientRole.setName("ROLE_PATIENT");
                patientRole = roleRepository.save(patientRole);
            }

            User user = User.builder().email("user2@test.com")
                    .password(passwordEncoder.encode("Valid@1234")).firstName("John")
                    .lastName("Smith").phone("+12345678903").age(28).gender(Gender.MALE)
                    .roles(Set.of(patientRole)).enabled(true)
                    .patientProfile(PatientProfile.builder().build()).build();
            return userRepository.save(user);
        }
    }

}
