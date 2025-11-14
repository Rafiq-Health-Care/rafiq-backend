package com.nexaworks.rafiq.integration.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.UserRegistrationRequest;
import com.nexaworks.rafiq.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DisplayName("User Controller Integration Test Cases")
public class UserControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Patient Registration")
    class PatientRegistration {
        private final String REGISTER_PATIENT_ENDPOINT = "/user/register/patient";

        @Nested
        @DisplayName("Should Register Patient Successfully")
        class ShouldRegisterPatientSuccessfully {

            @Test
            @DisplayName("Should register patient with all valid required fields")
            void shouldRegisterPatientWithValidRequiredFields() throws Exception {
                // Arrange
                String email = "john.doe.integration@example.com";
                UserRegistrationRequest request = new UserRegistrationRequest(email, "Valid@1234",
                        "John", "Doe", "+12345678901", 30, "male");

                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert HTTP response
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isCreated());

                // Verify persistence through the service layer
                assertTrue(userRepository.findByEmail(email).isPresent(),
                        "User should be persisted after registration");
            }
        }

        @Nested
        @DisplayName("Should Fail Registering Patient")
        class ShouldFailRegisteringPatient {

            @Test
            @DisplayName("Should return 400 Bad Request when email format is invalid")
            void shouldReturnBadRequestForInvalidEmail() throws Exception {
                String email = "not-an-email";
                UserRegistrationRequest invalidRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", 30, "male");

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid email
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "User should not be created for invalid request");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when password contains spaces")
            void shouldReturnBadRequestForInvalidPassword() throws Exception {
                String email = "valid.email@example.com";
                UserRegistrationRequest invalidRequest = new UserRegistrationRequest(email,
                        "Val id@1234", // Password with space
                        "John", "Doe", "+12345678901", 30, "male");

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid password
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "User should not be created for invalid password");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when phone number format is invalid")
            void shouldReturnBadRequestForInvalidPhone() throws Exception {
                String email = "test.phone@example.com";
                UserRegistrationRequest invalidRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "01234567890", // Invalid phone - starts with 0
                        30, "male");

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid phone
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "User should not be created for invalid phone");
            }

            @Test
            @DisplayName("Should return 409 Conflict when user with same email already exists")
            void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
                // Arrange - First register a patient
                String email = "duplicate@example.com";
                UserRegistrationRequest firstRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", 30, "male");

                String firstPayload = objectMapper.writeValueAsString(firstRequest);

                // Register first patient
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(firstPayload))
                        .andExpect(MockMvcResultMatchers.status().isCreated());

                // Verify first user was created
                assertTrue(userRepository.findByEmail(email).isPresent(),
                        "First user should be created");

                // Arrange - Try to register again with same email
                UserRegistrationRequest duplicateRequest = new UserRegistrationRequest(email,
                        "AnotherValid@1234", "Jane", "Smith", "+19876543210", 25, "female");

                String duplicatePayload = objectMapper.writeValueAsString(duplicateRequest);

                // Act & Assert - Second registration should fail with 409
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(duplicatePayload))
                        .andExpect(MockMvcResultMatchers.status().isConflict());

                // Ensure only one user exists
                assertTrue(userRepository.count() == 1,
                        "Should have only one user after duplicate attempt");
            }
        }
    }
}
