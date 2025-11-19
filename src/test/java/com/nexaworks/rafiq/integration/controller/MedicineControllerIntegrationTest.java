package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
import com.nexaworks.rafiq.dto.request.AddMedicineRequest;
import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Gender;
import com.nexaworks.rafiq.enums.MedicineFrequency;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.enums.MedicineType;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.DrugRepository;
import com.nexaworks.rafiq.repository.MedicineRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.UserRepository;

@DisplayName("Medicine Controller Integration Test")
public class MedicineControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Drug testDrug;

    @BeforeEach
    void setUp() {
        // Delete in correct order to avoid foreign key constraint violations
        medicineRepository.deleteAll();
        drugRepository.deleteAll();
        userRepository.deleteAll();
        patientRepository.deleteAll();

        // Create test user with patient profile
        testUser = createTestPatient("patient@example.com", "TestPass@123", "John", "Doe");

        // Create test drug
        testDrug = createTestDrug("Test Drug", "Test Group", "Tablet", "Oral", 10.5,
                "Test pharmacology");

    }

    private User createTestPatient(String email, String password, String firstName,
            String lastName) {
        // Get or create PATIENT role
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        // Create patient profile
        PatientProfile patientProfile = PatientProfile.builder().description("Test patient")
                .build();
        patientProfile = patientRepository.save(patientProfile);

        // Create user
        User user = User.builder().email(email).password(passwordEncoder.encode(password))
                .firstName(firstName).lastName(lastName).phone("+12345678901").age(30)
                .gender(Gender.MALE).roles(List.of(patientRole)).enabled(true)
                .patientProfile(patientProfile).build();
        user = userRepository.save(user);

        // Link patient profile to user
        patientProfile.setUser(user);
        patientRepository.save(patientProfile);

        return user;
    }

    private Drug createTestDrug(String tradeName, String drugGroup, String dosageForm, String route,
            double price, String pharmacology) {
        Drug drug = Drug.builder().tradeName(tradeName).drugGroup(drugGroup).dosageForm(dosageForm)
                .route(route).price(price).pharmacology(pharmacology)
                .activeIngredients(new ArrayList<>()).companies(new ArrayList<>()).build();
        return drugRepository.save(drug);
    }

    private Medicine createTestMedicine(User user, Drug drug) {
        Medicine medicine = Medicine.builder().dosage("500mg")
                .frequency(MedicineFrequency.TWICE_DAILY).status(MedicineStatus.ACTIVE)
                .type(MedicineType.TABLET).startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(86400 * 7)) // 7 days from now
                .notes("Test notes").drug(drug).patient(user.getPatientProfile()).build();
        return medicineRepository.save(medicine);
    }

    @Nested
    @DisplayName("Add Medicine")
    class AddMedicine {
        private final String ADD_MEDICINE_ENDPOINT = "/medicine/add";

        @Nested
        @DisplayName("Should Add Medicine Successfully")
        class ShouldAddMedicineSuccessfully {

            @Test
            @DisplayName("Should add medicine and return 201 when data is valid")
            void shouldAddMedicineAndReturnCreatedWhenDataIsValid() throws Exception {
                // Arrange
                AddMedicineRequest request = new AddMedicineRequest(testDrug.getId(), "500mg",
                        MedicineFrequency.TWICE_DAILY, Instant.now(),
                        Instant.now().plusSeconds(86400 * 7), // 7 days from now
                        "Take with food", MedicineType.TABLET);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(user(testUser))).andExpect(MockMvcResultMatchers.status().isCreated())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                .value("Medicine added successfully"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.data.dosage").value("500mg"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.data.frequency")
                                .value("TWICE_DAILY"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("ACTIVE"));

                // Verify medicine was saved in database
                long medicineCount = medicineRepository.count();
                assertThat(medicineCount).isEqualTo(1)
                        .as("Medicine should be saved in database after adding");
            }
        }

        @Nested
        @DisplayName("Should Fail Adding Medicine")
        class ShouldFailAddingMedicine {

            @Test
            @DisplayName("Should return 409 Conflict when medicine already exists")
            void shouldReturnConflictWhenMedicineAlreadyExists() throws Exception {
                Medicine existingMedicine = createTestMedicine(testUser, testDrug);

                testUser.getPatientProfile().setMedicines(List.of(existingMedicine));

                AddMedicineRequest request = new AddMedicineRequest(testDrug.getId(), "500mg",
                        MedicineFrequency.TWICE_DAILY, Instant.now(),
                        Instant.now().plusSeconds(86400 * 7), "Take with food",
                        MedicineType.TABLET);
                String payload = objectMapper.writeValueAsString(request);

                mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isConflict())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                .value("You already have this medicine"));

                long medicineCount = medicineRepository.count();
                assertThat(medicineCount).isEqualTo(1)
                        .as("No additional medicine should be saved when duplicate");
            }

            @Test
            @DisplayName("Should return 422 Unprocessable Entity when medicine limit is reached")
            void shouldReturnUnprocessableEntityWhenMedicineLimitIsReached() throws Exception {
                Drug anotherDrug = createTestDrug("Another Drug", "Group", "Capsule", "Oral", 15.0,
                        "Pharmacology");
                List<Medicine> medicines = new ArrayList<>();
                for (int i = 0; i < 200; i++) {
                    Drug drug = createTestDrug("Drug " + i, "Group", "Tablet", "Oral", 10.0 + i,
                            "Pharmacology");
                    Medicine medicine = createTestMedicine(testUser, drug);
                    medicines.add(medicine);
                }

                testUser.getPatientProfile().setMedicines(medicines);

                AddMedicineRequest request = new AddMedicineRequest(anotherDrug.getId(), "500mg",
                        MedicineFrequency.TWICE_DAILY, Instant.now(),
                        Instant.now().plusSeconds(86400 * 7), "Take with food",
                        MedicineType.TABLET);
                String payload = objectMapper.writeValueAsString(request);

                mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                .value("You have reached the maximum limit of medicines allowed."));

                long medicineCount = medicineRepository.count();
                assertThat(medicineCount).isEqualTo(200)
                        .as("No additional medicine should be saved when limit is reached");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when required fields are missing")
            void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
                String payload = """
                        {
                            "dosage": "500mg",
                            "frequency": "TWICE_DAILY",
                            "startDate": "%s"
                        }
                        """.formatted(Instant.now().toString());

                mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(medicineRepository.count()).isZero()
                        .as("No medicine should be saved when validation fails");
            }

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                AddMedicineRequest request = new AddMedicineRequest(testDrug.getId(), "500mg",
                        MedicineFrequency.TWICE_DAILY, Instant.now(),
                        Instant.now().plusSeconds(86400 * 7), "Take with food",
                        MedicineType.TABLET);
                String payload = objectMapper.writeValueAsString(request);

                mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                assertThat(medicineRepository.count()).isZero()
                        .as("No medicine should be saved when authentication fails");
            }
        }
    }
}
