package com.nexaworks.rafiq.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.user.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.UserRegistrationRequest;
import com.nexaworks.rafiq.dto.request.user.VerificationRequest;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.TokenType;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.SpecializationRepository;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.repository.UserRepository;

@DisplayName("User Controller Integration Test Cases")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
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
                        "John", "Doe", "+12345678901", "male", LocalDate.of(1999, 1, 1));

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
                        "Valid@1234", "John", "Doe", "+12345678901", "male",
                        LocalDate.of(1999, 1, 1));

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
                        "John", "Doe", "+12345678901", "male", LocalDate.of(1994, 1, 1));

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
                        "male", LocalDate.of(1994, 1, 1));

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
            @DisplayName("Should return 400 Bad Request when age is invalid")
            void shouldReturnBadRequestForInvalidAge() throws Exception {
                String email = "test.age@example.com";
                UserRegistrationRequest invalidRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "male",
                        LocalDate.now().plusDays(1)); // Invalid age - future
                                                      // date

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid age
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "User should not be created for invalid age");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when gender is invalid")
            void shouldReturnBadRequestForInvalidGender() throws Exception {
                String email = "test.gender@example.com";
                UserRegistrationRequest invalidRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "other",
                        LocalDate.of(1994, 1, 1)); // Invalid
                // gender

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid gender
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "User should not be created for invalid gender");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when first name is blank")
            void shouldReturnBadRequestForBlankFirstName() throws Exception {
                String email = "test.firstname@example.com";
                UserRegistrationRequest invalidRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "", // Blank first name
                        "Doe", "+12345678901", "male", LocalDate.of(1994, 1, 1));

                String payload = objectMapper.writeValueAsString(invalidRequest);

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with blank first name
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "User should not be created for blank first name");
            }

            @Test
            @DisplayName("Should return 409 Conflict when user with same email already exists")
            void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
                // Arrange - First register a patient
                String email = "duplicate@example.com";
                UserRegistrationRequest firstRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "male",
                        LocalDate.of(1994, 1, 1));

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
                        "AnotherValid@1234", "Jane", "Smith", "+19876543210", "female",
                        LocalDate.of(1999, 1, 1));

                String duplicatePayload = objectMapper.writeValueAsString(duplicateRequest);

                // Act & Assert - Second registration should fail with 409
                mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_PATIENT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(duplicatePayload))
                        .andExpect(MockMvcResultMatchers.status().isConflict());

                // Ensure only one user exists
                assertEquals(1, userRepository.count(),
                        "Should have only one user after duplicate attempt");
            }
        }
    }

    @Nested
    @DisplayName("Doctor Registration")
    class DoctorRegistration {
        private final String REGISTER_DOCTOR_ENDPOINT = "/user/register/doctor";

        @Nested
        @DisplayName("Should Register Doctor Successfully")
        class ShouldRegisterDoctorSuccessfully {

            @Test
            @DisplayName("Should register doctor with all valid required fields")
            void shouldRegisterDoctorWithValidRequiredFields() throws Exception {
                // Arrange
                String email = "dr.john.doe@example.com";
                UserRegistrationRequest userRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "male",
                        LocalDate.of(1994, 1, 1));

                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                        specializationId, "Experienced cardiologist with 10 years of practice");

                String doctorDataJson = objectMapper.writeValueAsString(request);
                MockMultipartFile doctorData = new MockMultipartFile("doctorData", "",
                        "application/json", doctorDataJson.getBytes());

                MockMultipartFile nationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", createMinimalPngImage());

                // Act & Assert HTTP response
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(doctorData).file(nationalId))
                        .andExpect(MockMvcResultMatchers.status().isCreated());

                // Verify persistence
                assertTrue(userRepository.findByEmail(email).isPresent(),
                        "Doctor should be persisted after registration");
            }
        }

        @Nested
        @DisplayName("Should Fail Registering Doctor")
        class ShouldFailRegisteringDoctor {

            @Test
            @DisplayName("Should return 400 Bad Request when email format is invalid")
            void shouldReturnBadRequestForInvalidEmail() throws Exception {
                // Arrange
                String email = "not-an-email";
                UserRegistrationRequest userRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "male",
                        LocalDate.of(1994, 1, 1));

                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                        specializationId, "Experienced cardiologist");

                String doctorDataJson = objectMapper.writeValueAsString(request);
                MockMultipartFile doctorData = new MockMultipartFile("doctorData", "",
                        "application/json", doctorDataJson.getBytes());

                MockMultipartFile nationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", "fake-image-content".getBytes());

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(doctorData).file(nationalId))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid email
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "Doctor should not be created for invalid email");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when password contains spaces")
            void shouldReturnBadRequestForInvalidPassword() throws Exception {
                // Arrange
                String email = "dr.valid.email@example.com";
                UserRegistrationRequest userRequest = new UserRegistrationRequest(email,
                        "Val id@1234", // Password with space
                        "John", "Doe", "+12345678901", "male", LocalDate.of(1994, 1, 1));

                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                        specializationId, "Experienced cardiologist");

                String doctorDataJson = objectMapper.writeValueAsString(request);
                MockMultipartFile doctorData = new MockMultipartFile("doctorData", "",
                        "application/json", doctorDataJson.getBytes());

                MockMultipartFile nationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", createMinimalPngImage());

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(doctorData).file(nationalId))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid password
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "Doctor should not be created for invalid password");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when phone number format is invalid")
            void shouldReturnBadRequestForInvalidPhone() throws Exception {
                // Arrange
                String email = "dr.test.phone@example.com";
                UserRegistrationRequest userRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "01234567890", // Invalid phone - starts with 0
                        "male", LocalDate.of(1994, 1, 1));

                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                        specializationId, "Experienced cardiologist");

                String doctorDataJson = objectMapper.writeValueAsString(request);
                MockMultipartFile doctorData = new MockMultipartFile("doctorData", "",
                        "application/json", doctorDataJson.getBytes());

                MockMultipartFile nationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", "fake-image-content".getBytes());

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(doctorData).file(nationalId))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid phone
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "Doctor should not be created for invalid phone");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when age is invalid")
            void shouldReturnBadRequestForInvalidAge() throws Exception {
                // Arrange
                String email = "dr.test.age@example.com";
                UserRegistrationRequest userRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "male",
                        LocalDate.now().plusDays(1));
                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                        specializationId, "Experienced cardiologist");

                String doctorDataJson = objectMapper.writeValueAsString(request);
                MockMultipartFile doctorData = new MockMultipartFile("doctorData", "",
                        "application/json", doctorDataJson.getBytes());

                MockMultipartFile nationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", "fake-image-content".getBytes());

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(doctorData).file(nationalId))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid age
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "Doctor should not be created for invalid age");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when gender is invalid")
            void shouldReturnBadRequestForInvalidGender() throws Exception {
                // Arrange
                String email = "dr.test.gender@example.com";
                UserRegistrationRequest userRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "other",
                        LocalDate.of(1994, 1, 1)); // Invalid
                // gender

                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                        specializationId, "Experienced cardiologist");

                String doctorDataJson = objectMapper.writeValueAsString(request);
                MockMultipartFile doctorData = new MockMultipartFile("doctorData", "",
                        "application/json", doctorDataJson.getBytes());

                MockMultipartFile nationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", "fake-image-content".getBytes());

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(doctorData).file(nationalId))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with the invalid gender
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "Doctor should not be created for invalid gender");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when first name is blank")
            void shouldReturnBadRequestForBlankFirstName() throws Exception {
                // Arrange
                String email = "dr.test.firstname@example.com";
                UserRegistrationRequest userRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "", // Blank first name
                        "Doe", "+12345678901", "male", LocalDate.of(1994, 1, 1));

                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                        specializationId, "Experienced cardiologist");

                String doctorDataJson = objectMapper.writeValueAsString(request);
                MockMultipartFile doctorData = new MockMultipartFile("doctorData", "",
                        "application/json", doctorDataJson.getBytes());

                MockMultipartFile nationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", "fake-image-content".getBytes());

                // Act & Assert HTTP response 400
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(doctorData).file(nationalId))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Ensure no user persisted with blank first name
                assertTrue(userRepository.findByEmail(email).isEmpty(),
                        "Doctor should not be created for blank first name");
            }

            @Test
            @DisplayName("Should return 409 Conflict when doctor with same email already exists")
            void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
                // Arrange - First register a doctor
                String email = "dr.duplicate@example.com";
                UserRegistrationRequest firstUserRequest = new UserRegistrationRequest(email,
                        "Valid@1234", "John", "Doe", "+12345678901", "male",
                        LocalDate.of(1994, 1, 1));

                UUID specializationId = specializationRepository.findAll().stream().findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("No specialization found in database"))
                        .getId();

                DoctorRegistrationRequest firstRequest = new DoctorRegistrationRequest(
                        firstUserRequest, specializationId, "Experienced cardiologist");

                String firstDoctorDataJson = objectMapper.writeValueAsString(firstRequest);
                MockMultipartFile firstDoctorData = new MockMultipartFile("doctorData", "",
                        "application/json", firstDoctorDataJson.getBytes());

                MockMultipartFile firstNationalId = new MockMultipartFile("nationalId",
                        "national-id.jpg", "image/jpeg", createMinimalPngImage());

                // Register first doctor
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(firstDoctorData).file(firstNationalId))
                        .andExpect(MockMvcResultMatchers.status().isCreated());

                // Verify first user was created
                assertTrue(userRepository.findByEmail(email).isPresent(),
                        "First doctor should be created");

                // Arrange - Try to register again with same email
                UserRegistrationRequest duplicateUserRequest = new UserRegistrationRequest(email,
                        "AnotherValid@1234", "Jane", "Smith", "+19876543210", "female",
                        LocalDate.of(1999, 1, 1));

                DoctorRegistrationRequest duplicateRequest = new DoctorRegistrationRequest(
                        duplicateUserRequest, specializationId, "Experienced surgeon");

                String duplicateDoctorDataJson = objectMapper.writeValueAsString(duplicateRequest);
                MockMultipartFile duplicateDoctorData = new MockMultipartFile("doctorData", "",
                        "application/json", duplicateDoctorDataJson.getBytes());

                MockMultipartFile duplicateNationalId = new MockMultipartFile("nationalId",
                        "national-id-2.jpg", "image/jpeg", "fake-image-content-2".getBytes());

                // Act & Assert - Second registration should fail with 409
                mockMvc.perform(MockMvcRequestBuilders.multipart(REGISTER_DOCTOR_ENDPOINT)
                        .file(duplicateDoctorData).file(duplicateNationalId))
                        .andExpect(MockMvcResultMatchers.status().isConflict());

                // Ensure only one user exists
                assertEquals(1, userRepository.count(),
                        "Should have only one user after duplicate attempt");
            }
        }
        private byte[] createMinimalPngImage() {
            return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG
                                                                                     // signature
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // 1x1 dimensions
                    0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00,
                    0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, // IDAT chunk
                    0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A,
                    0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, // IEND chunk
                    (byte) 0xAE, 0x42, 0x60, (byte) 0x82};
        }
    }

    @Nested
    @DisplayName("User Verification")
    class UserVerification {
        private final String VERIFICATION_ENDPOINT = "/user/verification";

        @Test
        @DisplayName("Should verify user with valid email and OTP")
        void shouldVerifyUserWithValidEmailAndOtp() throws Exception {
            // Arrange - Register a patient first
            String email = "verify.user@example.com";
            UserRegistrationRequest request = new UserRegistrationRequest(email, "Valid@1234",
                    "John", "Doe", "+12345678901", "male", LocalDate.of(1994, 1, 1));

            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post("/user/register/patient")
                    .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            // Get the generated OTP from database using TokenRepository
            User user = userRepository.findByEmail(email).orElseThrow();
            Token otpToken = tokenRepository.findAll().stream()
                    .filter(t -> t.getUser().getId().equals(user.getId()))
                    .filter(t -> t.getTokenType().equals(TokenType.OTP)).findFirst().orElseThrow();
            String otp = otpToken.getToken();

            // Prepare verification request
            VerificationRequest verificationRequest = new VerificationRequest(email, otp);
            String verificationPayload = objectMapper.writeValueAsString(verificationRequest);

            // Act & Assert - Verify the user
            mockMvc.perform(MockMvcRequestBuilders.post(VERIFICATION_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(verificationPayload))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.role").exists())
                    .andExpect(MockMvcResultMatchers.cookie().exists("jwt"))
                    .andExpect(MockMvcResultMatchers.cookie().exists("refreshToken"));

            // Verify user is now enabled
            User verifiedUser = userRepository.findByEmail(email).orElseThrow();
            assertTrue(verifiedUser.isEnabled(), "User should be enabled after verification");
        }

        @Test
        @DisplayName("Should return 404 Not Found when OTP does not exist")
        void shouldReturnNotFoundForInvalidOtp() throws Exception {
            // Arrange
            String email = "test@example.com";
            String fakeOtp = "999999";
            VerificationRequest request = new VerificationRequest(email, fakeOtp);

            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(VERIFICATION_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @DisplayName("New OTP")
    class NewOtp {
        private final String NEW_OTP_ENDPOINT = "/user/new-otp";

        @Test
        @DisplayName("Should generate new OTP for existing user")
        void shouldGenerateNewOtpForExistingUser() throws Exception {
            // Arrange - Register a patient first
            String email = "newotp@example.com";
            UserRegistrationRequest request = new UserRegistrationRequest(email, "Valid@1234",
                    "John", "Doe", "+12345678901", "male", LocalDate.of(1994, 1, 1));

            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(MockMvcRequestBuilders.post("/user/register/patient")
                    .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            // Get the old OTP token count
            User user = userRepository.findByEmail(email).orElseThrow();
            long oldOtpCount = tokenRepository.findAll().stream()
                    .filter(t -> t.getUser().getId().equals(user.getId()))
                    .filter(t -> t.getTokenType().equals(TokenType.OTP)).count();

            // Prepare new OTP request
            ForgetPasswordRequest newOtpRequest = new ForgetPasswordRequest(email);
            String newOtpPayload = objectMapper.writeValueAsString(newOtpRequest);

            // Act & Assert - Request new OTP
            mockMvc.perform(MockMvcRequestBuilders.post(NEW_OTP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(newOtpPayload))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            // Verify new OTP was generated
            long newOtpCount = tokenRepository.findAll().stream()
                    .filter(t -> t.getUser().getId().equals(user.getId()))
                    .filter(t -> t.getTokenType().equals(TokenType.OTP)).count();

            assertTrue(newOtpCount >= oldOtpCount, "New OTP should be generated");
        }

        @Test
        @DisplayName("Should return 200 OK even when user does not exist")
        void shouldReturnOkForNonExistentUser() throws Exception {
            // Arrange - Use non-existent email
            String email = "nonexistent@example.com";
            ForgetPasswordRequest request = new ForgetPasswordRequest(email);

            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert - Should return 200 OK (security: don't reveal if user exists)
            mockMvc.perform(MockMvcRequestBuilders.post(NEW_OTP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
    }
}
