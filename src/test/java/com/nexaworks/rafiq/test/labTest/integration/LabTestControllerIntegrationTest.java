package com.nexaworks.rafiq.test.labTest.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.ai.service.AiService;
import com.nexaworks.rafiq.labTest.api.dto.TestResultRequest;
import com.nexaworks.rafiq.labTest.entity.LabTest;
import com.nexaworks.rafiq.labTest.repository.LabResultRepository;
import com.nexaworks.rafiq.labTest.repository.LabTestRepository;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.repository.PatientRepository;
import com.nexaworks.rafiq.shared.dto.TestRequest;
import com.nexaworks.rafiq.test.BaseIntegrationTest;
import com.nexaworks.rafiq.user.entity.enums.Gender;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.repository.RoleRepository;
import com.nexaworks.rafiq.user.repository.UserRepository;

@DisplayName("Lab Test Controller Integration Test")
class LabTestControllerIntegrationTest extends BaseIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AiService mockAiService() throws Exception {
            AiService mockService = mock(AiService.class);
            // Mock extractLabResultsFromPdf method
            when(mockService.extractLabResultsFromPdf(any(byte[].class)))
                    .thenReturn("{\"name\":\"Test Result\",\"date\":\"2024-01-15\",\"tests\":[]}");
            // Mock analysisData method
            when(mockService.analysisData(any(String.class), any(List.class)))
                    .thenReturn("{\"analysis\":\"Mock analysis result\"}");
            return mockService;
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
        userRepository.deleteAll();
        patientRepository.deleteAll();

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

        // Create User first
        User user = User.builder().email(email).password(passwordEncoder.encode(password))
                .firstName(firstName).lastName(lastName).phone("+12345678901")
                .birthDate(LocalDate.of(1990, 1, 1)).gender(Gender.MALE)
                .roles(new java.util.HashSet<>(Set.of(patientRole))) // Use mutable HashSet
                .enabled(true).build();
        user = userRepository.save(user);

        // Create Patient with same ID
        Patient patient = Patient.builder().id(user.getId()).email(email).firstName(firstName)
                .lastName(lastName).phone("+12345678901").description("Test patient").build();
        patientRepository.save(patient);

        return user;
    }

    @Nested
    @DisplayName("Create Test with Results")
    class CreateTestWithResults {
        private final String TEST_RESULTS_ENDPOINT = "/lab-test/test-results";

        @Nested
        @DisplayName("Should Create Test Successfully")
        class ShouldCreateTestSuccessfully {

            @Test
            @DisplayName("Should create new test with results when data is valid")
            void shouldCreateNewTestWithResultsWhenDataIsValid() throws Exception {
                // Arrange - Prepare test results request (after file upload)
                UUID fileId = UUID.randomUUID(); // Simulating fileId from upload

                List<TestRequest> tests = new ArrayList<>();
                tests.add(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                tests.add(new TestRequest("WBC Count", 7500.0, "cells/μL", "Normal"));

                TestResultRequest request = new TestResultRequest("Complete Blood Count",
                        new Date(), tests, fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert - Create test with results
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify lab test was created
                assertThat(labTestRepository.count()).isEqualTo(1)
                        .as("One lab test should be created");

                // Verify lab results were saved
                assertThat(labResultRepository.count()).isEqualTo(2)
                        .as("Two lab results should be saved");

                // Verify test details
                LabTest createdTest = labTestRepository.findAll().get(0);
                assertThat(createdTest.getName()).isEqualTo("Complete Blood Count");
                assertThat(createdTest.getFileId()).isEqualTo(fileId);
            }

            @Test
            @DisplayName("Should create test with single result")
            void shouldCreateTestWithSingleResult() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                List<TestRequest> tests = List
                        .of(new TestRequest("Glucose", 95.0, "mg/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("Glucose Test", new Date(), tests,
                        fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify
                assertThat(labTestRepository.count()).isEqualTo(1);
                assertThat(labResultRepository.count()).isEqualTo(1);
            }

            @Test
            @DisplayName("Should create test with empty results list")
            void shouldCreateTestWithEmptyResultsList() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                List<TestRequest> emptyTests = new ArrayList<>();

                TestResultRequest request = new TestResultRequest("Empty Test", new Date(),
                        emptyTests, fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert - Should still save successfully
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify test created but no results
                assertThat(labTestRepository.count()).isEqualTo(1);
                assertThat(labResultRepository.count()).isZero();
            }
        }

        @Nested
        @DisplayName("Should Fail Creating Test")
        class ShouldFailCreatingTest {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert - Without authentication
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                // Verify nothing was created
                assertThat(labTestRepository.count()).isZero();
                assertThat(labResultRepository.count()).isZero();
            }

            @Test
            @DisplayName("Should return 400 Bad Request when name is blank")
            void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("", new Date(), tests, fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(labTestRepository.count()).isZero();
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test name is blank in TestRequest")
            void shouldReturnBadRequestWhenTestNameIsBlank() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                List<TestRequest> tests = List.of(new TestRequest("", 14.5, "g/dL", "Normal"));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when unit is blank in TestRequest")
            void shouldReturnBadRequestWhenUnitIsBlank() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "", "Normal"));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when status is blank in TestRequest")
            void shouldReturnBadRequestWhenStatusIsBlank() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                List<TestRequest> tests = List.of(new TestRequest("Hemoglobin", 14.5, "g/dL", ""));

                TestResultRequest request = new TestResultRequest("Blood Test", new Date(), tests,
                        fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESULTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("Get All Tests")
    class GetAllTests {
        private final String GET_ALL_ENDPOINT = "/lab-test";

        private LabTest createLabTest(String name, UUID fileId, User user) {
            LabTest labTest = LabTest.builder().name(name).description("Test description")
                    .fileId(fileId).patientId(user.getId()).date(Instant.now()).build();
            return labTestRepository.save(labTest);
        }

        @Nested
        @DisplayName("Should Get All Tests Successfully")
        class ShouldGetAllTestsSuccessfully {

            @Test
            @DisplayName("Should return paginated list of tests when user has tests")
            void shouldReturnPaginatedListOfTestsWhenUserHasTests() throws Exception {
                // Arrange - Create multiple lab tests for the user
                createLabTest("Blood Test 1", UUID.randomUUID(), testUser);
                createLabTest("Blood Test 2", UUID.randomUUID(), testUser);
                createLabTest("Urine Test", UUID.randomUUID(), testUser);

                // Act & Assert - Get all tests with default pagination
                mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].testId").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].fileId").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(3))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
            }

            @Test
            @DisplayName("Should return empty list when user has no tests")
            void shouldReturnEmptyListWhenUserHasNoTests() throws Exception {
                // Act & Assert - Get all tests when user has none
                mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(0))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(0))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
            }

            @Test
            @DisplayName("Should return paginated results with custom page size")
            void shouldReturnPaginatedResultsWithCustomPageSize() throws Exception {
                // Arrange - Create multiple tests
                for (int i = 1; i <= 5; i++) {
                    createLabTest("Test " + i, UUID.randomUUID(), testUser);
                }

                // Act & Assert - Get tests with page size 2
                mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).param("page", "0")
                        .param("size", "2").with(withUserId(testUser)))
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
                createLabTest("Zebra Test", UUID.randomUUID(), testUser);
                createLabTest("Alpha Test", UUID.randomUUID(), testUser);
                createLabTest("Beta Test", UUID.randomUUID(), testUser);

                // Act & Assert - Get tests sorted by name ascending
                mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_ENDPOINT).param("sort", "name")
                        .param("direction", "asc").with(withUserId(testUser)))
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

        private LabTest createLabTest(String name, UUID fileId, User user) {
            LabTest labTest = LabTest.builder().name(name).description("Test description")
                    .fileId(fileId).patientId(user.getId()).date(Instant.now()).build();
            return labTestRepository.save(labTest);
        }

        @Nested
        @DisplayName("Should Get Test Successfully")
        class ShouldGetTestSuccessfully {

            @Test
            @DisplayName("Should return test with results when test exists and belongs to user")
            void shouldReturnTestWithResultsWhenTestExistsAndBelongsToUser() throws Exception {
                // Arrange - Create lab test
                UUID fileId = UUID.randomUUID();
                LabTest labTest = createLabTest("Complete Blood Count", fileId, testUser);
                UUID testId = labTest.getId();

                // Act & Assert - Get test by ID
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, testId)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                                .value("Complete Blood Count"))
                        .andExpect(
                                MockMvcResultMatchers.jsonPath("$.testId").value(testId.toString()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.fileId").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.date").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.tests").isArray());
            }

            @Test
            @DisplayName("Should return test without results when test has no results")
            void shouldReturnTestWithoutResultsWhenTestHasNoResults() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                LabTest labTest = createLabTest("Empty Test", fileId, testUser);
                UUID testId = labTest.getId();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, testId)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Empty Test"))
                        .andExpect(
                                MockMvcResultMatchers.jsonPath("$.testId").value(testId.toString()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.fileId").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.date").exists())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.tests").isArray());
            }
        }

        @Nested
        @DisplayName("Should Fail Getting Test")
        class ShouldFailGettingTest {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", UUID.randomUUID(), testUser);
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
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, nonExistentId)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test belongs to different user")
            void shouldReturnBadRequestWhenTestBelongsToDifferentUser() throws Exception {
                // Arrange - Create another user and test
                User otherUser = createTestPatient("other@example.com", "OtherPass@123", "Other",
                        "User");
                LabTest otherUserTest = createLabTest("Other User Test", UUID.randomUUID(),
                        otherUser);
                UUID otherUserTestId = otherUserTest.getId();

                // Act & Assert - Try to access other user's test
                mockMvc.perform(MockMvcRequestBuilders.get(GET_BY_ID_ENDPOINT, otherUserTestId)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("Delete Test By ID")
    class DeleteTestById {
        private final String DELETE_BY_ID_ENDPOINT = "/lab-test/{test-id}";

        private LabTest createLabTest(String name, UUID fileId, User user) {
            LabTest labTest = LabTest.builder().name(name).description("Test description")
                    .fileId(fileId).patientId(user.getId()).date(Instant.now()).build();
            return labTestRepository.save(labTest);
        }

        @Nested
        @DisplayName("Should Delete Test Successfully")
        class ShouldDeleteTestSuccessfully {

            @Test
            @DisplayName("Should delete test and return 200 when test exists and belongs to user")
            void shouldDeleteTestAndReturnOkWhenTestExistsAndBelongsToUser() throws Exception {
                // Arrange - Create lab test
                LabTest labTest = createLabTest("Test to Delete", UUID.randomUUID(), testUser);
                UUID testId = labTest.getId();

                // Verify test exists
                assertThat(labTestRepository.findById(testId)).isPresent();

                // Act & Assert - Delete test
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_BY_ID_ENDPOINT, testId)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify test was deleted
                assertThat(labTestRepository.findById(testId)).isEmpty();
            }
        }

        @Nested
        @DisplayName("Should Fail Deleting Test")
        class ShouldFailDeletingTest {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                LabTest labTest = createLabTest("Test", UUID.randomUUID(), testUser);
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
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test belongs to different user")
            void shouldReturnBadRequestWhenTestBelongsToDifferentUser() throws Exception {
                // Arrange
                User otherUser = createTestPatient("other2@example.com", "OtherPass@123", "Other2",
                        "User");
                LabTest otherUserTest = createLabTest("Other User Test", UUID.randomUUID(),
                        otherUser);
                UUID otherUserTestId = otherUserTest.getId();

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders
                        .delete(DELETE_BY_ID_ENDPOINT, otherUserTestId).with(withUserId(testUser)))
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

        private LabTest createLabTest(String name, UUID fileId, User user) {
            LabTest labTest = LabTest.builder().name(name).description("Test description")
                    .fileId(fileId).patientId(user.getId()).date(Instant.now()).build();
            return labTestRepository.save(labTest);
        }

        @Nested
        @DisplayName("Should Delete All Tests Successfully")
        class ShouldDeleteAllTestsSuccessfully {

            @Test
            @DisplayName("Should delete all user's tests and return count")
            void shouldDeleteAllUserTestsAndReturnCount() throws Exception {
                // Arrange - Create multiple tests for the user
                createLabTest("Test 1", UUID.randomUUID(), testUser);
                createLabTest("Test 2", UUID.randomUUID(), testUser);
                createLabTest("Test 3", UUID.randomUUID(), testUser);

                // Verify tests exist
                assertThat(labTestRepository.count()).isEqualTo(3);

                // Act & Assert - Delete all tests
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_ALL_ENDPOINT)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$").value(3));
            }

            @Test
            @DisplayName("Should return 0 when user has no tests")
            void shouldReturnZeroWhenUserHasNoTests() throws Exception {
                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_ALL_ENDPOINT)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
            }

            @Test
            @DisplayName("Should only delete current user's tests, not other users' tests")
            void shouldOnlyDeleteCurrentUserTestsNotOtherUsersTests() throws Exception {
                createLabTest("My Test 1", UUID.randomUUID(), testUser);
                createLabTest("My Test 2", UUID.randomUUID(), testUser);

                User otherUser = createTestPatient("other3@example.com", "OtherPass@123", "Other3",
                        "User");
                createLabTest("Other User Test", UUID.randomUUID(), otherUser);

                // Verify all tests exist
                assertThat(labTestRepository.count()).isEqualTo(3);

                // Act - Delete all current user's tests
                mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_ALL_ENDPOINT)
                        .with(withUserId(testUser)))
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
                createLabTest("Test", UUID.randomUUID(), testUser);

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

        private LabTest createLabTest(String name, UUID fileId, User user) {
            LabTest labTest = LabTest.builder().name(name).description("Test description")
                    .fileId(fileId).patientId(user.getId()).date(Instant.now()).build();
            return labTestRepository.save(labTest);
        }

        @Nested
        @DisplayName("Should Update Test Successfully")
        class ShouldUpdateTestSuccessfully {

            @Test
            @DisplayName("Should update test name and results when data is valid")
            void shouldUpdateTestNameAndResultsWhenDataIsValid() throws Exception {
                // Arrange - Create test
                UUID fileId = UUID.randomUUID();
                LabTest labTest = createLabTest("Initial Test", fileId, testUser);
                UUID testId = labTest.getId();

                // Prepare update request
                List<TestRequest> updatedTests = new ArrayList<>();
                updatedTests.add(new TestRequest("Hemoglobin", 15.0, "g/dL", "Normal"));
                updatedTests.add(new TestRequest("WBC Count", 8000.0, "cells/μL", "High"));

                TestResultRequest updateRequest = new TestResultRequest("Updated Test Name",
                        new Date(), updatedTests, fileId);
                String updatePayload = objectMapper.writeValueAsString(updateRequest);

                // Act & Assert - Update test
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, testId)
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload)
                        .with(withUserId(testUser)))
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
                UUID fileId = UUID.randomUUID();
                LabTest labTest = createLabTest("Test to Update", fileId, testUser);
                UUID testId = labTest.getId();

                // Prepare update with empty results
                List<TestRequest> emptyTests = new ArrayList<>();
                TestResultRequest updateRequest = new TestResultRequest("Updated Name", new Date(),
                        emptyTests, fileId);
                String updatePayload = objectMapper.writeValueAsString(updateRequest);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, testId)
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify test updated
                LabTest updatedTest = labTestRepository.findById(testId).orElseThrow();
                assertThat(updatedTest.getName()).isEqualTo("Updated Name");
            }
        }

        @Nested
        @DisplayName("Should Fail Updating Test")
        class ShouldFailUpdatingTest {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                LabTest labTest = createLabTest("Test", fileId, testUser);
                UUID testId = labTest.getId();

                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("Updated", new Date(), tests,
                        fileId);
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
                UUID fileId = UUID.randomUUID();
                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("Updated", new Date(), tests,
                        fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when test belongs to different user")
            void shouldReturnBadRequestWhenTestBelongsToDifferentUser() throws Exception {
                // Arrange
                User otherUser = createTestPatient("other4@example.com", "OtherPass@123", "Other4",
                        "User");
                UUID fileId = UUID.randomUUID();
                LabTest otherUserTest = createLabTest("Other User Test", fileId, otherUser);
                UUID otherUserTestId = otherUserTest.getId();

                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("Updated", new Date(), tests,
                        fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, otherUserTestId)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when name is blank")
            void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
                // Arrange
                UUID fileId = UUID.randomUUID();
                LabTest labTest = createLabTest("Test", fileId, testUser);
                UUID testId = labTest.getId();

                List<TestRequest> tests = List
                        .of(new TestRequest("Hemoglobin", 14.5, "g/dL", "Normal"));
                TestResultRequest request = new TestResultRequest("", new Date(), tests, fileId);
                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ENDPOINT, testId)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        }
    }
}
