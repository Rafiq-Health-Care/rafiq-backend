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

import jakarta.persistence.EntityManager;

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
    EntityManager entityManager;
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

}
