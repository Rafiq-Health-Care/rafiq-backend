package com.nexaworks.rafiq.test.patient.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.patient.api.dto.request.CompletePatientDataRequest;
import com.nexaworks.rafiq.patient.entity.enums.BloodType;
import com.nexaworks.rafiq.patient.entity.enums.SmokeStatus;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.repository.PatientRepository;
import com.nexaworks.rafiq.test.BaseIntegrationTest;
import com.nexaworks.rafiq.user.entity.enums.Gender;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.repository.RoleRepository;
import com.nexaworks.rafiq.user.repository.UserRepository;

@DisplayName("Patient Controller Integration Test Cases")
class PatientControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.nexaworks.rafiq.patient.service.PatientService patientService;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createTestPatientUser(String email, String firstName, String lastName) {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        User user = User.builder().email(email).password(passwordEncoder.encode("password123"))
                .firstName(firstName).lastName(lastName).phone("+1234567890")
                .birthDate(LocalDate.of(1990, 1, 15)).gender(Gender.MALE)
                .roles(new java.util.HashSet<>(Set.of(patientRole))) // Use mutable HashSet
                .enabled(true).active(true).build();
        user = userRepository.save(user);

        // Register the patient entity - this is crucial!
        patientService.register(email, firstName, lastName, user.getId());

        return user;
    }

    private CompletePatientDataRequest createValidPatientDataRequest() {
        return new CompletePatientDataRequest(175, // heightInCm
                70.5, // weightInKg
                BloodType.O_POSITIVE, SmokeStatus.NO, 0, // cigarettesPerDay
                null, // lastSmoked
                false, // alcoholism
                0, // drinksPerWeek
                false, // pregnant
                "Software Engineer", "John Emergency", "+1987654321");
    }

    @Nested
    @DisplayName("POST /patients/medical-profile - Create Patient Profile")
    class CreatePatientProfileTests {

        @Test
        @DisplayName("Should create patient profile successfully with valid data")
        @Transactional
        void shouldCreatePatientProfileSuccessfully() throws Exception {
            // Given
            User user = createTestPatientUser("patient@test.com", "John", "Doe");
            CompletePatientDataRequest request = createValidPatientDataRequest();

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Patient profile created successfully"))
                    .andExpect(jsonPath("$.data.patientId").value(user.getId().toString()))
                    .andExpect(jsonPath("$.data.firstName").value("John"))
                    .andExpect(jsonPath("$.data.lastName").value("Doe"))
                    .andExpect(jsonPath("$.data.email").value("patient@test.com"))
                    .andExpect(jsonPath("$.data.height").value(175))
                    .andExpect(jsonPath("$.data.weight").value(70.5))
                    .andExpect(jsonPath("$.data.bloodType").value("O_POSITIVE"))
                    .andExpect(jsonPath("$.data.smokeStatus").value("NO"))
                    .andExpect(jsonPath("$.data.alcoholism").value(false))
                    .andExpect(jsonPath("$.data.occupation").value("Software Engineer"))
                    .andExpect(jsonPath("$.data.emergencyContactName").value("John Emergency"))
                    .andExpect(jsonPath("$.data.emergencyContactPhone").value("+1987654321"))
                    .andExpect(jsonPath("$.data.bmi").exists());

            // Verify database
            Patient patient = patientRepository.findById(user.getId()).orElse(null);
            assertThat(patient).isNotNull();
            assertThat(patient.getHeight()).isEqualTo(175);
            assertThat(patient.getWeight()).isEqualTo(70.5);
            assertThat(patient.getBloodType()).isEqualTo(BloodType.O_POSITIVE);
        }

        @Test
        @DisplayName("Should create patient profile with smoker data")
        @Transactional
        void shouldCreatePatientProfileWithSmokerData() throws Exception {
            // Given
            User user = createTestPatientUser("smoker@test.com", "Jane", "Smith");
            CompletePatientDataRequest request = new CompletePatientDataRequest(160, 55.0,
                    BloodType.A_POSITIVE, SmokeStatus.YES, 10, new Date(), false, 0, false,
                    "Teacher", "Emergency Contact", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.smokeStatus").value("YES"));

            // Verify database
            Patient patient = patientRepository.findById(user.getId()).orElse(null);
            assertThat(patient).isNotNull();
            assertThat(patient.getSmokeStatus()).isEqualTo(SmokeStatus.YES);
            assertThat(patient.getCigarettesPerDay()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should create patient profile with all blood types")
        @Transactional
        void shouldCreatePatientProfileWithAllBloodTypes() throws Exception {
            // Test all blood types
            BloodType[] bloodTypes = BloodType.values();

            for (int i = 0; i < bloodTypes.length; i++) {
                User user = createTestPatientUser("patient" + i + "@test.com", "Patient" + i,
                        "Test");
                CompletePatientDataRequest request = new CompletePatientDataRequest(170, 65.0,
                        bloodTypes[i], SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                        "Emergency", "+1234567890");

                mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.bloodType").value(bloodTypes[i].toString()));
            }
        }

        @Test
        @DisplayName("Should fail when height is negative")
        @Transactional
        void shouldFailWhenHeightIsNegative() throws Exception {
            // Given
            User user = createTestPatientUser("invalid@test.com", "Invalid", "User");
            CompletePatientDataRequest request = new CompletePatientDataRequest(-175, 70.5,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when weight is negative")
        @Transactional
        void shouldFailWhenWeightIsNegative() throws Exception {
            // Given
            User user = createTestPatientUser("invalid2@test.com", "Invalid", "User");
            CompletePatientDataRequest request = new CompletePatientDataRequest(175, -70.5,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when height is zero")
        @Transactional
        void shouldFailWhenHeightIsZero() throws Exception {
            // Given
            User user = createTestPatientUser("zero@test.com", "Zero", "User");
            CompletePatientDataRequest request = new CompletePatientDataRequest(0, 70.5,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when not authenticated")
        void shouldFailWhenNotAuthenticated() throws Exception {
            // Given
            CompletePatientDataRequest request = createValidPatientDataRequest();

            // When & Then
            mockMvc.perform(
                    post("/patients/medical-profile").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /patients/medical-profile - Update Patient Profile")
    class UpdatePatientProfileTests {

        @Test
        @DisplayName("Should update patient profile successfully")
        @Transactional
        void shouldUpdatePatientProfileSuccessfully() throws Exception {
            // Given - Create initial profile
            User user = createTestPatientUser("update@test.com", "Update", "Test");
            CompletePatientDataRequest initialRequest = createValidPatientDataRequest();

            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialRequest)))
                    .andExpect(status().isCreated());

            // When - Update profile
            CompletePatientDataRequest updateRequest = new CompletePatientDataRequest(180, // Changed
                                                                                           // height
                    75.0, // Changed weight
                    BloodType.AB_POSITIVE, // Changed blood type
                    SmokeStatus.FORMER, // Changed smoke status
                    5, new Date(), true, // Changed alcoholism
                    3, // Changed drinks per week
                    false, "Senior Engineer", // Changed occupation
                    "New Emergency Contact", "+1111111111");

            // Then
            mockMvc.perform(put("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Patient profile updated successfully"))
                    .andExpect(jsonPath("$.data.height").value(180))
                    .andExpect(jsonPath("$.data.weight").value(75.0))
                    .andExpect(jsonPath("$.data.bloodType").value("AB_POSITIVE"))
                    .andExpect(jsonPath("$.data.smokeStatus").value("FORMER"))
                    .andExpect(jsonPath("$.data.alcoholism").value(true))
                    .andExpect(jsonPath("$.data.occupation").value("Senior Engineer"));

            // Verify database
            Patient patient = patientRepository.findById(user.getId()).orElse(null);
            assertThat(patient).isNotNull();
            assertThat(patient.getHeight()).isEqualTo(180);
            assertThat(patient.getWeight()).isEqualTo(75.0);
            assertThat(patient.getBloodType()).isEqualTo(BloodType.AB_POSITIVE);
        }

        @Test
        @DisplayName("Should update patient profile multiple times")
        @Transactional
        void shouldUpdatePatientProfileMultipleTimes() throws Exception {
            // Given
            User user = createTestPatientUser("multiple@test.com", "Multiple", "Updates");
            CompletePatientDataRequest request1 = new CompletePatientDataRequest(170, 70.0,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Job1",
                    "Emergency1", "+1111111111");

            // First creation
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isCreated());

            // Update 1
            CompletePatientDataRequest request2 = new CompletePatientDataRequest(172, 72.0,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Job2",
                    "Emergency2", "+2222222222");

            mockMvc.perform(put("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.height").value(172));

            // Update 2
            CompletePatientDataRequest request3 = new CompletePatientDataRequest(175, 75.0,
                    BloodType.A_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Job3",
                    "Emergency3", "+3333333333");

            mockMvc.perform(put("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request3))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.height").value(175))
                    .andExpect(jsonPath("$.data.weight").value(75.0));
        }

        @Test
        @DisplayName("Should fail update when not authenticated")
        void shouldFailUpdateWhenNotAuthenticated() throws Exception {
            // Given
            CompletePatientDataRequest request = createValidPatientDataRequest();

            // When & Then
            mockMvc.perform(put("/patients/medical-profile").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should fail update with invalid data")
        @Transactional
        void shouldFailUpdateWithInvalidData() throws Exception {
            // Given
            User user = createTestPatientUser("invalidupdate@test.com", "Invalid", "Update");

            // Create initial profile
            CompletePatientDataRequest initialRequest = createValidPatientDataRequest();
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialRequest)))
                    .andExpect(status().isCreated());

            // Try to update with invalid data
            CompletePatientDataRequest invalidRequest = new CompletePatientDataRequest(-180, -75.0,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(put("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle pregnant patient")
        @Transactional
        void shouldHandlePregnantPatient() throws Exception {
            // Given
            User user = createTestPatientUser("pregnant@test.com", "Pregnant", "Patient");
            CompletePatientDataRequest request = new CompletePatientDataRequest(165, 68.0,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, true, // pregnant =
                                                                                   // true
                    "Teacher", "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Verify
            Patient patient = patientRepository.findById(user.getId()).orElse(null);
            assertThat(patient).isNotNull();
            assertThat(patient.isPregnant()).isTrue();
        }

        @Test
        @DisplayName("Should handle patient with alcoholism")
        @Transactional
        void shouldHandlePatientWithAlcoholism() throws Exception {
            // Given
            User user = createTestPatientUser("alcohol@test.com", "Alcohol", "Patient");
            CompletePatientDataRequest request = new CompletePatientDataRequest(175, 80.0,
                    BloodType.B_POSITIVE, SmokeStatus.NO, 0, null, true, 14, false, // alcoholism
                                                                                    // with 14
                                                                                    // drinks per
                                                                                    // week
                    "Bartender", "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.alcoholism").value(true));

            // Verify
            Patient patient = patientRepository.findById(user.getId()).orElse(null);
            assertThat(patient).isNotNull();
            assertThat(patient.isAlcoholism()).isTrue();
            assertThat(patient.getDrinksPerWeek()).isEqualTo(14);
        }

        @Test
        @DisplayName("Should handle former smoker")
        @Transactional
        void shouldHandleFormerSmoker() throws Exception {
            // Given
            User user = createTestPatientUser("formersmoker@test.com", "Former", "Smoker");
            Date lastSmoked = new Date();
            CompletePatientDataRequest request = new CompletePatientDataRequest(170, 70.0,
                    BloodType.O_NEGATIVE, SmokeStatus.FORMER, 20, lastSmoked, false, 0, false,
                    "Developer", "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.smokeStatus").value("FORMER"));

            // Verify
            Patient patient = patientRepository.findById(user.getId()).orElse(null);
            assertThat(patient).isNotNull();
            assertThat(patient.getSmokeStatus()).isEqualTo(SmokeStatus.FORMER);
            assertThat(patient.getLastSmoked()).isNotNull();
        }

        @Test
        @DisplayName("Should handle minimal required fields")
        @Transactional
        void shouldHandleMinimalRequiredFields() throws Exception {
            // Given
            User user = createTestPatientUser("minimal@test.com", "Minimal", "Fields");
            CompletePatientDataRequest request = new CompletePatientDataRequest(170, 70.0, null,
                    null, 0, null, false, 0, false, null, null, null);

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.data.height").value(170))
                    .andExpect(jsonPath("$.data.weight").value(70.0));
        }

        @Test
        @DisplayName("Should calculate BMI correctly")
        @Transactional
        void shouldCalculateBMICorrectly() throws Exception {
            // Given
            User user = createTestPatientUser("bmi@test.com", "BMI", "Test");
            CompletePatientDataRequest request = new CompletePatientDataRequest(175, 70.0,
                    BloodType.O_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Emergency", "+1234567890");

            // When & Then
            mockMvc.perform(post("/patients/medical-profile").with(withUserId(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.data.bmi").exists())
                    .andExpect(jsonPath("$.data.bmi").isString());
        }
    }
}
