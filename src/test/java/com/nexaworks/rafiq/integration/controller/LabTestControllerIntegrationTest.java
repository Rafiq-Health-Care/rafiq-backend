package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.nexaworks.rafiq.entities.LabTest;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Gender;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.LabResultRepository;
import com.nexaworks.rafiq.repository.LabTestRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.AiService;

@DisplayName("Lab Test Controller Integration Test")
public class LabTestControllerIntegrationTest extends BaseIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AiService mockAiService() {
            // Return a mock implementation that returns predictable results
            return new AiService() {
                @Override
                public String extractLabResultsFromPdf(byte[] pdfFile)
                        throws java.io.IOException, com.itextpdf.text.DocumentException {
                    // Return a default mock response
                    return "{\"name\":\"Test Result\",\"date\":\"2024-01-15\",\"tests\":[]}";
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private LabResultRepository labResultRepository;

    @Autowired
    private PatientRepository patientRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Delete in correct order to avoid foreign key constraint violations
        labResultRepository.deleteAll();
        labTestRepository.deleteAll();
        userRepository.deleteAll(); // Delete users first (they reference patient profiles)
        patientRepository.deleteAll(); // Then delete patient profiles

        // Create test user with patient profile
        testUser = createTestPatient("patient@example.com", "TestPass@123", "John", "Doe");
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

    private LabTest createLabTest(String name, User user) {
        LabTest labTest = LabTest.builder().name(name).description("Test description")
                .patient(user.getPatientProfile()).date(Instant.now()).build();
        return labTestRepository.save(labTest);
    }

    @Nested
    @DisplayName("Upload Lab Test")
    class UploadLabTest {
        private final String UPLOAD_ENDPOINT = "/lab-test/upload";

        @Nested
        @DisplayName("Should Upload Successfully")
        class ShouldUploadSuccessfully {

            @Test
            @DisplayName("Should upload PDF and return extracted results when file is valid")
            void shouldUploadPdfAndReturnExtractedResultsWhenFileIsValid() throws Exception {
                // Arrange - Create a mock PDF file
                MockMultipartFile pdfFile = new MockMultipartFile("file", "test-lab-result.pdf",
                        "application/pdf", "PDF content here".getBytes());

                // Act & Assert - Upload PDF with authentication
                // The mock AI service will return predictable results
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(pdfFile)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.content()
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.testId").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Result"));

                // Verify lab test was saved in database
                long labTestCount = labTestRepository.count();
                assertThat(labTestCount).isEqualTo(1)
                        .as("Lab test should be saved in database after upload");
            }

            @Test
            @DisplayName("Should upload image file and convert to PDF then extract results")
            void shouldUploadImageAndConvertToPdfThenExtractResults() throws Exception {
                // Arrange - Create a mock image file
                MockMultipartFile imageFile = new MockMultipartFile("file", "lab-result.jpg",
                        "image/jpeg", createMinimalPngImage());

                // Act & Assert - Upload image with authentication
                // The mock AI service will return predictable results
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(imageFile)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.content()
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.testId").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Result"));

                // Verify lab test was saved
                assertThat(labTestRepository.count()).isEqualTo(1);
            }
            private byte[] createMinimalPngImage() {
                return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG
                        // signature
                        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk
                        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // 1x1 dimensions
                        0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00,
                        0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, // IDAT chunk
                        0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0D,
                        0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, // IEND
                                                                                                 // chunk
                        (byte) 0xAE, 0x42, 0x60, (byte) 0x82};
            }
        }

        @Nested
        @DisplayName("Should Fail Upload")
        class ShouldFailUpload {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange - Create a mock PDF file
                MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf",
                        "application/pdf", "PDF content".getBytes());

                // Act & Assert - Upload without authentication
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(pdfFile))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                // Verify no lab test was saved
                assertThat(labTestRepository.count()).isZero()
                        .as("No lab test should be saved when authentication fails");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when file is empty")
            void shouldReturnBadRequestWhenFileIsEmpty() throws Exception {
                // Arrange - Create an empty file
                MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf",
                        "application/pdf", new byte[0]);

                // Act & Assert - Upload empty file with authentication
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(emptyFile)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no lab test was saved
                assertThat(labTestRepository.count()).isZero()
                        .as("No lab test should be saved for empty file");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when file parameter is missing")
            void shouldReturnBadRequestWhenFileParameterMissing() throws Exception {
                // Act & Assert - Upload without file parameter
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no lab test was saved
                assertThat(labTestRepository.count()).isZero();
            }
        }
    }
}
