package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
import com.nexaworks.rafiq.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.VerifyOtpRequest;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Gender;
import com.nexaworks.rafiq.enums.TokenType;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.repository.UserRepository;

@DisplayName("Auth Controller Integration Test Cases")
public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createTestUser(String email, String password, String firstName, String lastName) {
        // Get or create PATIENT role
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        User user = User.builder().email(email).password(passwordEncoder.encode(password))
                .firstName(firstName).lastName(lastName).phone("+12345678901").age(30)
                .gender(Gender.MALE).roles(java.util.List.of(patientRole)).enabled(true).build();
        return userRepository.save(user);
    }

    private Token createOtpToken(User user, String otpValue, Instant expiryDate) {
        Token token = Token.builder().token(otpValue).user(user).tokenType(TokenType.OTP)
                .expiryDate(expiryDate).build();
        return tokenRepository.save(token);
    }

    private Token createAccessToken(User user, String tokenValue, Instant expiryDate) {
        Token token = Token.builder().token(tokenValue).user(user).tokenType(TokenType.ACCESS_TOKEN)
                .expiryDate(expiryDate).build();
        return tokenRepository.save(token);
    }

    @Nested
    @DisplayName("Forget Password")
    class ForgetPassword {
        private final String FORGET_PASSWORD_ENDPOINT = "/auth/forget-password";

        @Nested
        @DisplayName("Should Process Forget Password Successfully")
        class ShouldProcessForgetPasswordSuccessfully {

            @Test
            @DisplayName("Should generate OTP and return 200 OK when user exists")
            void shouldGenerateOtpAndReturnOkWhenUserExists() throws Exception {
                // Arrange - Create a test user directly
                String email = "forget.password@example.com";
                User user = createTestUser(email, "Valid@1234", "John", "Doe");

                // Get the initial OTP count
                long initialOtpCount = tokenRepository.findAll().stream()
                        .filter(t -> t.getUser().getId().equals(user.getId()))
                        .filter(t -> t.getTokenType().equals(TokenType.OTP)).count();

                // Prepare forget password request
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(email);
                String forgetPasswordPayload = objectMapper
                        .writeValueAsString(forgetPasswordRequest);

                // Act & Assert - Request forget password
                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(forgetPasswordPayload))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify new OTP was generated using AssertJ
                long newOtpCount = tokenRepository.findAll().stream()
                        .filter(t -> t.getUser().getId().equals(user.getId()))
                        .filter(t -> t.getTokenType().equals(TokenType.OTP)).count();

                assertThat(newOtpCount).isGreaterThan(initialOtpCount)
                        .as("New OTP should be generated for forget password");
            }

            @Test
            @DisplayName("Should return 200 OK when user does not exist (security best practice)")
            void shouldReturnOkWhenUserDoesNotExist() throws Exception {
                // Arrange - Use non-existent email
                String email = "nonexistent.user@example.com";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(email);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                // Act & Assert - Should return 200 OK (security: don't reveal if user exists)
                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                // Verify no user exists with this email using AssertJ
                assertThat(userRepository.findByEmail(email)).isEmpty().as("User should not exist");

                // Verify no OTP was generated
                long otpCount = tokenRepository.findAll().stream()
                        .filter(t -> t.getTokenType().equals(TokenType.OTP)).count();

                assertThat(otpCount).isZero()
                        .as("No OTP should be generated for non-existent user");
            }
        }

        @Nested
        @DisplayName("Should Fail Processing Forget Password")
        class ShouldFailProcessingForgetPassword {

            @Test
            @DisplayName("Should return 400 Bad Request when email format is invalid")
            void shouldReturnBadRequestForInvalidEmail() throws Exception {
                // Arrange
                String invalidEmail = "not-an-email";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(
                        invalidEmail);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                // Act & Assert - Should return 400 Bad Request
                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no OTP was generated using AssertJ
                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for invalid email");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when email is blank")
            void shouldReturnBadRequestForBlankEmail() throws Exception {
                // Arrange
                String blankEmail = "";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(blankEmail);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                // Act & Assert - Should return 400 Bad Request
                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no OTP was generated using AssertJ
                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for blank email");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when email is null")
            void shouldReturnBadRequestForNullEmail() throws Exception {
                // Arrange - Create JSON manually with null email
                String payload = "{\"email\":null}";

                // Act & Assert - Should return 400 Bad Request
                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no OTP was generated using AssertJ
                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for null email");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when email has whitespace only")
            void shouldReturnBadRequestForWhitespaceEmail() throws Exception {
                // Arrange
                String whitespaceEmail = "   ";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(
                        whitespaceEmail);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                // Act & Assert - Should return 400 Bad Request
                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no OTP was generated using AssertJ
                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for whitespace email");
            }
        }
    }

    @Nested
    @DisplayName("Verify OTP")
    class VerifyOtp {
        private final String VERIFY_OTP_ENDPOINT = "/auth/verify";

        @Nested
        @DisplayName("Should Verify OTP Successfully")
        class ShouldVerifyOtpSuccessfully {

            @Test
            @DisplayName("Should verify OTP and return access token when OTP is valid")
            void shouldVerifyOtpAndReturnAccessTokenWhenOtpIsValid() throws Exception {
                // Arrange - Create user and OTP token directly
                String email = "verify.user@example.com";
                User user = createTestUser(email, "Valid@1234", "Jane", "Smith");

                String otpValue = "123456";
                Instant expiryDate = Instant.now().plus(15, ChronoUnit.MINUTES);
                createOtpToken(user, otpValue, expiryDate);

                // Prepare verify OTP request
                VerifyOtpRequest verifyRequest = new VerifyOtpRequest(email, otpValue);
                String payload = objectMapper.writeValueAsString(verifyRequest);

                // Act & Assert - Verify OTP
                mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists());
            }
        }

        @Nested
        @DisplayName("Should Fail Verifying OTP")
        class ShouldFailVerifyingOtp {

            @Test
            @DisplayName("Should return 404 Not Found when OTP does not exist")
            void shouldReturnNotFoundWhenOtpDoesNotExist() throws Exception {
                // Arrange - Create user but no OTP token
                String email = "no.otp@example.com";
                createTestUser(email, "Valid@1234", "John", "Doe");

                String fakeOtp = "999999";
                VerifyOtpRequest verifyRequest = new VerifyOtpRequest(email, fakeOtp);
                String payload = objectMapper.writeValueAsString(verifyRequest);

                // Act & Assert - Should return 404
                mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isNotFound());
            }

            @Test
            @DisplayName("Should return 401 unauthorized when OTP is expired")
            void shouldReturnBadRequestWhenOtpIsExpired() throws Exception {
                // Arrange - Create user and expired OTP token
                String email = "expired.otp@example.com";
                User user = createTestUser(email, "Valid@1234", "Bob", "Wilson");

                String otpValue = "654321";
                Instant expiredDate = Instant.now().minus(1, ChronoUnit.HOURS); // Expired 1 hour
                                                                                // ago
                createOtpToken(user, otpValue, expiredDate);

                VerifyOtpRequest verifyRequest = new VerifyOtpRequest(email, otpValue);
                String payload = objectMapper.writeValueAsString(verifyRequest);

                // Act & Assert - Should return 400
                mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            }

            @Test
            @DisplayName("Should return 401 unauthorized when email does not match OTP")
            void shouldReturnBadRequestWhenEmailDoesNotMatchOtp() throws Exception {
                // Arrange - Create user and OTP, but use different email
                String correctEmail = "correct@example.com";
                User user = createTestUser(correctEmail, "Valid@1234", "Alice", "Johnson");

                String otpValue = "111222";
                Instant expiryDate = Instant.now().plus(15, ChronoUnit.MINUTES);
                createOtpToken(user, otpValue, expiryDate);

                // Try to verify with wrong email
                String wrongEmail = "wrong@example.com";
                VerifyOtpRequest verifyRequest = new VerifyOtpRequest(wrongEmail, otpValue);
                String payload = objectMapper.writeValueAsString(verifyRequest);

                // Act & Assert - Should return 400
                mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            }

            @Test
            @DisplayName("Should return 400 Bad Request when OTP format is invalid (validation)")
            void shouldReturnBadRequestForInvalidOtpFormat() throws Exception {
                // Arrange
                String email = "test@example.com";
                String invalidOtp = "12345"; // Only 5 digits, should be 6

                VerifyOtpRequest verifyRequest = new VerifyOtpRequest(email, invalidOtp);
                String payload = objectMapper.writeValueAsString(verifyRequest);

                // Act & Assert - Should return 400 for validation error
                mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("Change Password")
    class ChangePassword {
        private final String CHANGE_PASSWORD_ENDPOINT = "/auth/change-password";

        @Nested
        @DisplayName("Should Change Password Successfully")
        class ShouldChangePasswordSuccessfully {

            @Test
            @DisplayName("Should change password and return 204 when access token is valid")
            void shouldChangePasswordAndReturnNoContentWhenAccessTokenIsValid() throws Exception {
                // Arrange - Create user and access token directly
                String email = "change.password@example.com";
                String oldPassword = "OldPass@123";
                User user = createTestUser(email, oldPassword, "Mike", "Johnson");

                String accessTokenValue = "access-token-12345";
                Instant expiryDate = Instant.now().plus(30, ChronoUnit.MINUTES);
                createAccessToken(user, accessTokenValue, expiryDate);

                String newPassword = "NewPass@456";
                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                        accessTokenValue, newPassword);
                String payload = objectMapper.writeValueAsString(changePasswordRequest);

                // Act & Assert - Change password
                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isNoContent());

                // Verify password was changed
                User updatedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue()
                        .as("Password should be updated to new password");
            }
        }

        @Nested
        @DisplayName("Should Fail Changing Password")
        class ShouldFailChangingPassword {

            @Test
            @DisplayName("Should return 404 Not Found when access token does not exist")
            void shouldReturnNotFoundWhenAccessTokenDoesNotExist() throws Exception {
                // Arrange
                String fakeAccessToken = "fake-access-token-99999";
                String newPassword = "NewPass@456";

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                        fakeAccessToken, newPassword);
                String payload = objectMapper.writeValueAsString(changePasswordRequest);

                // Act & Assert - Should return 404
                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isNotFound());
            }

            @Test
            @DisplayName("Should return 401 Unauthorized when access token is expired")
            void shouldReturnUnauthorizedWhenAccessTokenIsExpired() throws Exception {
                // Arrange - Create user and expired access token
                String email = "expired.token@example.com";
                User user = createTestUser(email, "OldPass@123", "Tom", "Davis");

                String accessTokenValue = "expired-access-token";
                Instant expiredDate = Instant.now().minus(1, ChronoUnit.HOURS); // Expired
                createAccessToken(user, accessTokenValue, expiredDate);

                String newPassword = "NewPass@456";
                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                        accessTokenValue, newPassword);
                String payload = objectMapper.writeValueAsString(changePasswordRequest);

                // Act & Assert - Should return 401
                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                // Verify password was NOT changed
                User unchangedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches("OldPass@123", unchangedUser.getPassword()))
                        .isTrue().as("Password should remain unchanged");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when password is too short (validation)")
            void shouldReturnBadRequestForInvalidPasswordLength() throws Exception {
                // Arrange - Create user and valid access token
                String email = "short.password@example.com";
                User user = createTestUser(email, "OldPass@123", "Sarah", "Miller");

                String accessTokenValue = "valid-access-token";
                Instant expiryDate = Instant.now().plus(30, ChronoUnit.MINUTES);
                createAccessToken(user, accessTokenValue, expiryDate);

                String shortPassword = "Short1"; // Only 6 chars, min is 8

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                        accessTokenValue, shortPassword);
                String payload = objectMapper.writeValueAsString(changePasswordRequest);

                // Act & Assert - Should return 400 for validation error
                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify password was NOT changed
                User unchangedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches("OldPass@123", unchangedUser.getPassword()))
                        .isTrue().as("Password should remain unchanged");
            }
        }
    }
}
