package com.nexaworks.rafiq.integration.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.entities.enums.BloodType;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.MedicineType;
import com.nexaworks.rafiq.entities.enums.SmokeStatus;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.*;
import com.nexaworks.rafiq.user.entity.enums.Gender;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.repository.RoleRepository;
import com.nexaworks.rafiq.user.repository.UserRepository;

@DisplayName("Patient Profile Controller Integration Test Cases")
public class PatientProfileControllerIntegrationTest extends BaseIntegrationTest {

    private static final String PATIENT_PROFILE_ENDPOINT = "/patients/medical-profile";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    LabTestRepository labTestRepository;
    @Autowired
    MedicineRepository medicineRepository;
    @Autowired
    DrugRepository drugRepository;

    @BeforeEach
    void setUp() {
        labTestRepository.deleteAll();
        medicineRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Patient createTestPatient() {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        Patient patient = Patient.builder().email("patient@test.com")
                .password(passwordEncoder.encode("Valid@1234")).firstName("John").lastName("Doe")
                .phone("+12345678901").gender(Gender.MALE).enabled(true).roles(Set.of(patientRole))
                .build();
        return patientRepository.save(patient);
    }

    private CreateBasicMedicalProfileRequest buildValidProfileRequest() {
        return new CreateBasicMedicalProfileRequest(180, 75.0, BloodType.A_POSITIVE, SmokeStatus.NO,
                0, null, false, 0, false, "Engineer", "Jane Doe", "+10987654321");
    }

    @Nested
    @DisplayName("POST /patients/medical-profile - Create/Complete basic medical profile")
    class CreatePatientProfile {

        @Test
        @DisplayName("Should create patient profile successfully with valid request")
        void shouldCreatePatientProfile_WhenRequestIsValid() throws Exception {
            User patient = createTestPatient();
            CreateBasicMedicalProfileRequest request = buildValidProfileRequest();

            mockMvc.perform(MockMvcRequestBuilders.post(PATIENT_PROFILE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)).with(withUserId(patient)))
                    .andExpect(status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Patient profile created successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.patientId")
                            .value(patient.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.height").value(180))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.bloodType").value("A_POSITIVE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.smokeStatus").value("NO"))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.occupation").value("Engineer"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.emergencyContactName")
                            .value("Jane Doe"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.emergencyContactPhone")
                            .value("+10987654321"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when height is negative")
        void shouldReturnBadRequest_WhenHeightIsNegative() throws Exception {
            User patient = createTestPatient();
            CreateBasicMedicalProfileRequest invalidRequest = new CreateBasicMedicalProfileRequest(
                    -10, 75.0, BloodType.A_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false,
                    "Engineer", "Jane Doe", "+10987654321");

            mockMvc.perform(MockMvcRequestBuilders.post(PATIENT_PROFILE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
                    .with(withUserId(patient))).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /patients/medical-profile - Get complete patient profile")
    class GetPatientProfile {

        @Test
        @DisplayName("Should return complete patient profile with tests and medicines")
        void shouldReturnCompletePatientProfile_WithTestsAndMedicines() throws Exception {
            Patient patient = createTestPatient();

            // Seed some basic medical profile data
            patient.setHeight(175);
            patient.setWeight(70);
            patient.setBloodType(BloodType.O_POSITIVE);
            patient.setSmokeStatus(SmokeStatus.FORMER);
            patient.setCigarettesPerDay(5);
            patient.setLastSmoked(new Date());
            patient.setAlcoholism(false);
            patient.setDrinksPerWeek(2);
            patient.setPregnant(false);
            patient.setOccupation("Teacher");
            patient.setEmergencyContactName("Alice");
            patient.setEmergencyContactPhone("+19876543210");
            patientRepository.save(patient);

            // Seed a lab test and a medicine to verify nested lists
            LabTest labTest = LabTest.builder().patient(patient).name("Blood Test").build();
            labTestRepository.save(labTest);
            Drug drug = Drug.builder().build();
            drugRepository.save(drug);

            Medicine medicine = Medicine.builder().patient(patient).name("Aspirin").dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).status(MedicineStatus.ACTIVE)
                    .type(MedicineType.PRESCRIPTION).drug(drug).build();
            medicineRepository.save(medicine);

            mockMvc.perform(
                    MockMvcRequestBuilders.get(PATIENT_PROFILE_ENDPOINT).with(withUserId(patient)))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.patientProfile").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.patientProfile.patientId")
                            .value(patient.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.tests").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.tests.length()").value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.tests[0].name").value("Blood Test"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicines").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.medicines.length()").value(1))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.medicines[0].name").value("Aspirin"));
        }
    }

    @Nested
    @DisplayName("PUT /patients/medical-profile - Update basic medical profile")
    class UpdatePatientProfile {

        @Test
        @DisplayName("Should update patient profile successfully with valid request")
        void shouldUpdatePatientProfile_WhenRequestIsValid() throws Exception {
            Patient patient = createTestPatient();

            // Initial profile values
            patient.setHeight(170);
            patient.setWeight(65);
            patient.setBloodType(BloodType.B_POSITIVE);
            patientRepository.save(patient);

            CreateBasicMedicalProfileRequest updateRequest = new CreateBasicMedicalProfileRequest(
                    185, 80.0, BloodType.AB_NEGATIVE, SmokeStatus.YES, 10, new Date(), true, 5,
                    false, "Doctor", "Bob Smith", "+1122334455");

            mockMvc.perform(MockMvcRequestBuilders.put(PATIENT_PROFILE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .with(withUserId(patient))).andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Patient profile updated successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.height").value(185))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.data.bloodType").value("AB_NEGATIVE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.smokeStatus").value("YES"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.alcoholism").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.occupation").value("Doctor"));
        }
    }
}
