package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.*;

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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.labTest.TestRequest;
import com.nexaworks.rafiq.dto.request.labTest.TestResultRequest;
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

    @Autowired
    private ObjectMapper objectMapper;

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

        // Create patient profile (don't save yet)
        PatientProfile patientProfile = PatientProfile.builder().description("Test patient")
                .build();

        // Create user with patient profile
        User user = User.builder().email(email).password(passwordEncoder.encode(password))
                .firstName(firstName).lastName(lastName).phone("+12345678901").age(30)
                .gender(Gender.MALE).roles(Set.of(patientRole)).enabled(true)
                .patientProfile(patientProfile).build();

        // Set bidirectional relationship
        patientProfile.setUser(user);

        // Save user (will cascade to patientProfile)
        return userRepository.save(user);
    }

    private LabTest createLabTest(String name, User user) {
        LabTest labTest = LabTest.builder().name(name).description("Test description")
                .patient(user.getPatientProfile()).date(Instant.now()).build();
        return labTestRepository.save(labTest);
    }

    @Nested
    @DisplayName("Upload Lab Test")
    @Transactional
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
                return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00,
                        0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00,
                        0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                        (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78,
                        (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A,
                        0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
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

    @Nested
    @DisplayName("Test Results")
    class TestResults {
        private final String TEST_RESULTS_ENDPOINT = "/lab-test/test-results";

        @Nested
        @DisplayName("Should Save Test Results Successfully")
        class ShouldSaveTestResultsSuccessfully {

            @Test
            @DisplayName("Should save test results and return 200 when data is valid")
            void shouldSaveTestResultsAndReturnOkWhenDataIsValid() throws Exception {
                // Arrange - Create a lab test first
                LabTest labTest = createLabTest("Blood Test", testUser);
                UUID testId = labTest.getId();

                // Prepare test results request
                List<TestRequest> tests = new ArrayList<>();
                tests.add(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                tests.add(new TestRequest("WBC Count", 7500.0, "cells/μL", "Normal"));

                TestResultRequest request = new TestResultRequest("Complete Blood Count",
                        new Date(), tests, testId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert - Save test results with authentication
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify lab results were saved
                long labResultCount = labResultRepository.count();
                assertThat(labResultCount).isEqualTo(2)
                        .as("Two lab results should be saved in database");

                // Verify lab test was updated
                LabTest updatedTest = labTestRepository.findById(testId).orElseThrow();
                assertThat(updatedTest.getName()).isEqualTo("Complete Blood Count");
            }

            @Test
            @DisplayName("Should save test results with single test")
            void shouldSaveTestResultsWithSingleTest() throws Exception {
                // Arrange - Create a lab test
                LabTest labTest = createLabTest("Initial Test", testUser);
                UUID testId = labTest.getId();

                // Single test result
                List<TestRequest> tests = List
                        .of(new TestRequest("Glucose", 95.0, "mg/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("Glucose Test", new Date(), tests,
                        testId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify
                assertThat(labResultRepository.count()).isEqualTo(1);
            }

            @Test
            @DisplayName("Should save test results with empty tests list")
            void shouldSaveTestResultsWithEmptyTestsList() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Empty Results Test", testUser);
                UUID testId = labTest.getId();

                List<TestRequest> emptyTests = new ArrayList<>();

                TestResultRequest request = new TestResultRequest("Empty Test", new Date(),
                        emptyTests, testId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert - Should still save successfully
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify no lab results created but test was updated
                assertThat(labResultRepository.count()).isZero();
            }
        }

        @Nested
        @DisplayName("Should Fail Saving Test Results")
        class ShouldFailSavingTestResults {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        labTest.getId());
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert - Without authentication
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                // Verify no results were saved
                assertThat(labResultRepository.count()).isZero();
            }

            @Test
            @DisplayName("Should return 400 Bad Request when name is blank")
            void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("", new Date(), tests,
                        labTest.getId());
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert - Should return 400 for validation error
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no results were saved
                assertThat(labResultRepository.count()).isZero();
            }

            @Test
            @DisplayName("Should return 400 Bad Request when name is null")
            void shouldReturnBadRequestWhenNameIsNull() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);

                // Create JSON manually with null name
                String payload = String.format(
                        "{\"name\":null,\"date\":\"%s\",\"tests\":[{\"testName\":\"Hemoglobin\",\"result\":14.5,\"unit\":\"g/dL\",\"status\":\"Normal\"}],\"testId\":\"%s\"}",
                        new Date().getTime(), labTest.getId().toString());

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test name is blank in TestRequest")
            void shouldReturnBadRequestWhenTestNameIsBlank() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                List<TestRequest> tests = List.of(new TestRequest("", 14.5, "g/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        labTest.getId());
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when unit is blank in TestRequest")
            void shouldReturnBadRequestWhenUnitIsBlank() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "", "Normal"));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        labTest.getId());
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when status is blank in TestRequest")
            void shouldReturnBadRequestWhenStatusIsBlank() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                List<TestRequest> tests = List.of(new TestRequest("Hemoglobin", 14.5, "g/dL", ""));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        labTest.getId());
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("Get All Tests")
    class GetAllTests {
        private final String GET_ALL_ENDPOINT = "/lab-test";

        @Nested
        @DisplayName("Should Get All Tests Successfully")
        class ShouldGetAllTestsSuccessfully {

            @Test
            @DisplayName("Should return paginated list of tests when user has tests")
            void shouldReturnPaginatedListOfTestsWhenUserHasTests() throws Exception {
                // Arrange - Create multiple lab tests for the user
                createLabTest("Blood Test 1", testUser);
                createLabTest("Blood Test 2", testUser);
                createLabTest("Urine Test", testUser);

                // Act & Assert - Get all tests with default pagination
                mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(3))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
            }

            @Test
            @DisplayName("Should return empty list when user has no tests")
            void shouldReturnEmptyListWhenUserHasNoTests() throws Exception {
                // Act & Assert - Get all tests when user has none
                mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(0))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(0));
            }

            @Test
            @DisplayName("Should return paginated results with custom page size")
            void shouldReturnPaginatedResultsWithCustomPageSize() throws Exception {
                // Arrange - Create multiple tests
                for (int i = 1; i <= 5; i++) {
                    createLabTest("Test " + i, testUser);
                }

                // Act & Assert - Get tests with page size 2
                mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).param("page", "0")
                        .param("size", "2")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(2))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(false));
            }

            @Test
            @DisplayName("Should return sorted results by name")
            void shouldReturnSortedResultsByName() throws Exception {
                // Arrange - Create tests with different names
                createLabTest("Zebra Test", testUser);
                createLabTest("Alpha Test", testUser);
                createLabTest("Beta Test", testUser);

                // Act & Assert - Get tests sorted by name ascending
                mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).param("sort", "name")
                        .param("direction", "asc")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name")
                                .value("Alpha Test"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name")
                                .value("Beta Test"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name")
                                .value("Zebra Test"));
            }
        }

        @Nested
        @DisplayName("Should Fail Getting All Tests")
        class ShouldFailGettingAllTests {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Act & Assert - Without authentication
                mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_ENDPOINT))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("Get Test By ID")
    class GetTestById {
        private final String GET_BY_ID_ENDPOINT = "/lab-test/{test-id}";

        @Nested
        @DisplayName("Should Get Test Successfully")
        class ShouldGetTestSuccessfully {

            @Test
            @DisplayName("Should return test with results when test exists and belongs to user")
            void shouldReturnTestWithResultsWhenTestExistsAndBelongsToUser() throws Exception {
                // Arrange - Create lab test with results
                LabTest labTest = createLabTest("Complete Blood Count", testUser);
                UUID testId = labTest.getId();

                // Add test results
                List<TestRequest> tests = List.of(
                        new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"),
                        new TestRequest("WBC", 7500.0, "cells/μL", "Normal"));
                TestResultRequest request = new TestResultRequest("Complete Blood Count",
                        new Date(), tests, testId);
                String payload = objectMapper.writeValueAsString(request);

                mockMvc.perform(MockMvcRequestBuilders.post("/lab-test/test-results")
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Act & Assert - Get test by ID
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, testId).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                                .value("Complete Blood Count"))
                        .andExpect(
                                MockMvcResultMatchers.jsonPath("$.testId").value(testId.toString()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.tests").isArray())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.tests.length()").value(2));
            }

            @Test
            @DisplayName("Should return test without results when test has no results")
            void shouldReturnTestWithoutResultsWhenTestHasNoResults() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Empty Test", testUser);
                UUID testId = labTest.getId();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, testId).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Empty Test"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.testId")
                                .value(testId.toString()));
            }
        }

        @Nested
        @DisplayName("Should Fail Getting Test")
        class ShouldFailGettingTest {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                UUID testId = labTest.getId();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, testId))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test ID does not exist")
            void shouldReturnBadRequestWhenTestIdDoesNotExist() throws Exception {
                // Arrange - Use non-existent test ID
                UUID nonExistentId = UUID.randomUUID();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, nonExistentId).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test belongs to different user")
            void shouldReturnBadRequestWhenTestBelongsToDifferentUser() throws Exception {
                // Arrange - Create another user and test
                User otherUser = createTestPatient("other@example.com", "OtherPass@123", "Other",
                        "User");
                LabTest otherUserTest = createLabTest("Other User Test", otherUser);
                UUID otherUserTestId = otherUserTest.getId();

                // Act & Assert - Try to access other user's test
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, otherUserTestId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("Delete Test By ID")
    class DeleteTestById {
        private final String DELETE_BY_ID_ENDPOINT = "/lab-test/{test-id}";

        @Nested
        @DisplayName("Should Delete Test Successfully")
        class ShouldDeleteTestSuccessfully {

            @Test
            @DisplayName("Should delete test and return 200 when test exists and belongs to user")
            void shouldDeleteTestAndReturnOkWhenTestExistsAndBelongsToUser() throws Exception {
                // Arrange - Create lab test
                LabTest labTest = createLabTest("Test to Delete", testUser);
                UUID testId = labTest.getId();

                // Verify test exists
                assertThat(labTestRepository.findById(testId)).isPresent();

                // Act & Assert - Delete test
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_BY_ID_ENDPOINT, testId).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify test was deleted
                assertThat(labTestRepository.findById(testId)).isEmpty()
                        .as("Test should be deleted from database");
            }

            @Test
            @DisplayName("Should delete test with associated results")
            void shouldDeleteTestWithAssociatedResults() throws Exception {
                // Arrange - Create test with results
                LabTest labTest = createLabTest("Test with Results", testUser);
                UUID testId = labTest.getId();

                // Add results
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("Test with Results", new Date(),
                        tests, testId);
                String payload = objectMapper.writeValueAsString(request);

                mockMvc.perform(MockMvcRequestBuilders.post("/lab-test/test-results")
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify results exist
                assertThat(labResultRepository.count()).isEqualTo(1);

                // Act - Delete test
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_BY_ID_ENDPOINT, testId).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify test and results were deleted (cascade)
                assertThat(labTestRepository.findById(testId)).isEmpty();
                assertThat(labResultRepository.count()).isZero();
            }
        }

        @Nested
        @DisplayName("Should Fail Deleting Test")
        class ShouldFailDeletingTest {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                UUID testId = labTest.getId();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_BY_ID_ENDPOINT, testId))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                // Verify test still exists
                assertThat(labTestRepository.findById(testId)).isPresent();
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test ID does not exist")
            void shouldReturnBadRequestWhenTestIdDoesNotExist() throws Exception {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_BY_ID_ENDPOINT, nonExistentId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test belongs to different user")
            void shouldReturnBadRequestWhenTestBelongsToDifferentUser() throws Exception {
                // Arrange
                User otherUser = createTestPatient("other2@example.com", "OtherPass@123", "Other2",
                        "User");
                LabTest otherUserTest = createLabTest("Other User Test", otherUser);
                UUID otherUserTestId = otherUserTest.getId();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders
                        .delete(DELETE_BY_ID_ENDPOINT, otherUserTestId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify other user's test still exists
                assertThat(labTestRepository.findById(otherUserTestId)).isPresent();
            }
        }
    }

    @Nested
    @DisplayName("Delete All Tests")
    class DeleteAllTests {
        private final String DELETE_ALL_ENDPOINT = "/lab-test";

        @Nested
        @DisplayName("Should Delete All Tests Successfully")
        class ShouldDeleteAllTestsSuccessfully {

            @Test
            @DisplayName("Should delete all user's tests and return count")
            void shouldDeleteAllUserTestsAndReturnCount() throws Exception {
                // Arrange - Create multiple tests for the user
                createLabTest("Test 1", testUser);
                createLabTest("Test 2", testUser);
                createLabTest("Test 3", testUser);

                // Verify tests exist
                assertThat(labTestRepository.count()).isEqualTo(3);

                // Act & Assert - Delete all tests
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_ALL_ENDPOINT).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$").value(3));

            }

            @Test
            @DisplayName("Should return 0 when user has no tests")
            void shouldReturnZeroWhenUserHasNoTests() throws Exception {
                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_ALL_ENDPOINT).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
            }

            @Test
            @DisplayName("Should only delete current user's tests, not other users' tests")
            void shouldOnlyDeleteCurrentUserTestsNotOtherUsersTests() throws Exception {
                createLabTest("My Test 1", testUser);
                createLabTest("My Test 2", testUser);

                User otherUser = createTestPatient("other3@example.com", "OtherPass@123", "Other3",
                        "User");
                createLabTest("Other User Test", otherUser);

                // Verify all tests exist
                assertThat(labTestRepository.count()).isEqualTo(3);

                // Act - Delete all current user's tests
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_ALL_ENDPOINT).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$").value(2));

            }
        }

        @Nested
        @DisplayName("Should Fail Deleting All Tests")
        class ShouldFailDeletingAllTests {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                createLabTest("Test", testUser);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_ALL_ENDPOINT))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                // Verify test still exists
                assertThat(labTestRepository.count()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("Update Test")
    class UpdateTest {
        private final String UPDATE_ENDPOINT = "/lab-test/update/{test-id}";

        @Nested
        @DisplayName("Should Update Test Successfully")
        class ShouldUpdateTestSuccessfully {

            @Test
            @DisplayName("Should update test name and results when data is valid")
            void shouldUpdateTestNameAndResultsWhenDataIsValid() throws Exception {
                // Arrange - Create test with initial results
                LabTest labTest = createLabTest("Initial Test", testUser);
                UUID testId = labTest.getId();

                // Add initial results
                List<TestRequest> initialTests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest initialRequest = new TestResultRequest("Initial Test", new Date(),
                        initialTests, testId);
                String initialPayload = objectMapper.writeValueAsString(initialRequest);

                mockMvc.perform(MockMvcRequestBuilders.post("/lab-test/test-results")
                        .contentType(MediaType.APPLICATION_JSON).content(initialPayload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Prepare update request
                List<TestRequest> updatedTests = new ArrayList<>();
                updatedTests.add(new TestRequest("Hemoglobin", 15.0, "g/dL", "Normal"));
                updatedTests.add(new TestRequest("WBC Count", 8000.0, "cells/μL", "High"));

                TestResultRequest updateRequest = new TestResultRequest("Updated Test Name",
                        new Date(), updatedTests, testId);
                String updatePayload = objectMapper.writeValueAsString(updateRequest);

                // Act & Assert - Update test
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, testId)
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify test was updated
                LabTest updatedTest = labTestRepository.findById(testId).orElseThrow();
                assertThat(updatedTest.getName()).isEqualTo("Updated Test Name");

                // Verify results were updated
                assertThat(labResultRepository.count()).isEqualTo(2);
            }

            @Test
            @DisplayName("Should update test with empty results list")
            void shouldUpdateTestWithEmptyResultsList() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test to Update", testUser);
                UUID testId = labTest.getId();

                // Add initial results
                List<TestRequest> initialTests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest initialRequest = new TestResultRequest("Test to Update",
                        new Date(), initialTests, testId);
                String initialPayload = objectMapper.writeValueAsString(initialRequest);

                mockMvc.perform(MockMvcRequestBuilders.post("/lab-test/test-results")
                        .contentType(MediaType.APPLICATION_JSON).content(initialPayload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Prepare update with empty results
                List<TestRequest> emptyTests = new ArrayList<>();
                TestResultRequest updateRequest = new TestResultRequest("Updated Name", new Date(),
                        emptyTests, testId);
                String updatePayload = objectMapper.writeValueAsString(updateRequest);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, testId)
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify results were cleared
                assertThat(labResultRepository.count()).isZero();
            }
        }

        @Nested
        @DisplayName("Should Fail Updating Test")
        class ShouldFailUpdatingTest {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                UUID testId = labTest.getId();
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("Updated", new Date(), tests,
                        testId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, testId)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test ID does not exist")
            void shouldReturnBadRequestWhenTestIdDoesNotExist() throws Exception {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("Updated", new Date(), tests,
                        nonExistentId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test belongs to different user")
            void shouldReturnBadRequestWhenTestBelongsToDifferentUser() throws Exception {
                // Arrange
                User otherUser = createTestPatient("other4@example.com", "OtherPass@123", "Other4",
                        "User");
                LabTest otherUserTest = createLabTest("Other User Test", otherUser);
                UUID otherUserTestId = otherUserTest.getId();

                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("Updated", new Date(), tests,
                        otherUserTestId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, otherUserTestId)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when name is blank")
            void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", testUser);
                UUID testId = labTest.getId();
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("", new Date(), tests, testId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, testId)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        }
    }
}