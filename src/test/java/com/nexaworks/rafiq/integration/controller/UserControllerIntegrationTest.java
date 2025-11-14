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
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

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
                UserRegistrationRequest request =
                        new UserRegistrationRequest(email, "Valid@1234", "John", "Doe", "+12345678901", 30, "male");

                String payload = objectMapper.writeValueAsString(request);

                // Act & Assert HTTP response
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                        .andExpect(MockMvcResultMatchers.status().isCreated());

                // Verify persistence through the service layer
                assertTrue(
                        userRepository.findByEmail(email).isPresent(), "User should be persisted after registration");
            }
        }

        @Nested
        @DisplayName("Should Fail Registering Patient")
        class ShouldFailRegisteringPatient {

            @Test
            @DisplayName("Should return 400 Bad Request when email format is invalid")
            void shouldReturnBadRequestForInvalidEmail() throws Exception {
                String email = "not-an-email";
                UserRegistrationRequest invalidRequest =
                        new UserRegistrationRequest(email, "Valid@1234", "John", "Doe", "+12345678901", 30, "male");

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid email
                assertTrue(
                        userRepository.findByEmail(email).isEmpty(), "User should not be created for invalid request");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when password contains spaces")
            void shouldReturnBadRequestForInvalidPassword() throws Exception {
                String email = "valid.email@example.com";
                UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
                        email,
                        "Val id@1234", // Password with space
                        "John",
                        "Doe",
                        "+12345678901",
                        30,
                        "male");

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid password
                assertTrue(
                        userRepository.findByEmail(email).isEmpty(), "User should not be created for invalid password");
            }
        }
    }
}
