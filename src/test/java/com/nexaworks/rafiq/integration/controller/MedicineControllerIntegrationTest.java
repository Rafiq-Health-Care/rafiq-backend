package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.medicine.AddMedicineRequest;
import com.nexaworks.rafiq.entities.*;
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
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
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
                    MedicineFrequency.ONCE, null, null, Instant.now(), null, null, null);

            String payload = objectMapper.writeValueAsString(request);

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
        @DisplayName("Should return 422 Unprocessable Entity when user exceeds medicine limit")
        void shouldReturnUnprocessableEntity_WhenUserExceedsMedicineLimit() throws Exception {
            User user = createTestUser();
            Drug drug = createDrug();

            AddMedicineRequest request = new AddMedicineRequest(drug.getId(), "100mg",
                    MedicineFrequency.ONCE, null, null, Instant.now(), null, null, null);

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
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
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
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
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
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
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
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
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
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
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
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT)
                    .with(SecurityMockMvcRequestPostProcessors.user(user1)))
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
                    .param("page", "0").param("size", "2")
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
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
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_ALL_MEDICINES_ENDPOINT).param("status", "ACTIVE")
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.content[0].status").value("ACTIVE"));
        }
    }
}
