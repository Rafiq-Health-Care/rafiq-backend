package com.nexaworks.rafiq.test.user.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import com.nexaworks.rafiq.test.BaseIntegrationTest;
import com.nexaworks.rafiq.user.api.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ResetPasswordRequest;
import com.nexaworks.rafiq.user.entity.enums.Gender;
import com.nexaworks.rafiq.user.entity.enums.TokenType;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.Token;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.repository.RoleRepository;
import com.nexaworks.rafiq.user.repository.TokenRepository;
import com.nexaworks.rafiq.user.repository.UserRepository;

@DisplayName("Password Controller Integration Test Cases")
class PasswordControllerIntegrationTest extends BaseIntegrationTest {

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
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        User user = User.builder().email(email).password(passwordEncoder.encode(password))
                .firstName(firstName).lastName(lastName).phone("+12345678901")
                .birthDate(LocalDate.of(1990, 1, 1)).gender(Gender.MALE).roles(Set.of(patientRole))
                .enabled(true).build();
        return userRepository.save(user);
    }

    private Token createAccessToken(User user, String tokenValue, Instant expiryDate) {
        Token token = Token.builder().token(tokenValue).user(user).tokenType(TokenType.ACCESS_TOKEN)
                .expiryDate(expiryDate).build();
        return tokenRepository.save(token);
    }

    @Nested
    @DisplayName("Forget Password")
    class ForgetPassword {
        private final String FORGET_PASSWORD_ENDPOINT = "/password/forget-password";

        @Nested
        @DisplayName("Should Process Forget Password Successfully")
        class ShouldProcessForgetPasswordSuccessfully {

            @Test
            @DisplayName("Should generate AccessToken and return 200 OK when user exists")
            void shouldGenerateOtpAndReturnOkWhenUserExists() throws Exception {
                String email = "forget.password@example.com";
                User user = createTestUser(email, "Valid@1234", "John", "Doe");

                long initialOtpCount = tokenRepository.findAll().stream()
                        .filter(t -> t.getUser().getId().equals(user.getId()))
                        .filter(t -> t.getTokenType().equals(TokenType.ACCESS_TOKEN)).count();

                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(email);
                String forgetPasswordPayload = objectMapper
                        .writeValueAsString(forgetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(forgetPasswordPayload))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                long newOtpCount = tokenRepository.findAll().stream()
                        .filter(t -> t.getUser().getId().equals(user.getId()))
                        .filter(t -> t.getTokenType().equals(TokenType.ACCESS_TOKEN)).count();

                assertThat(newOtpCount).isGreaterThan(initialOtpCount)
                        .as("New OTP should be generated for forget password");
            }

            @Test
            @DisplayName("Should return 200 OK when user does not exist (security best practice)")
            void shouldReturnOkWhenUserDoesNotExist() throws Exception {
                String email = "nonexistent.user@example.com";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(email);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                assertThat(userRepository.findByEmail(email)).isEmpty().as("User should not exist");

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
                String invalidEmail = "not-an-email";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(
                        invalidEmail);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for invalid email");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when email is blank")
            void shouldReturnBadRequestForBlankEmail() throws Exception {
                String blankEmail = "";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(blankEmail);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for blank email");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when email is null")
            void shouldReturnBadRequestForNullEmail() throws Exception {
                String payload = "{\"email\":null}";

                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for null email");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when email has whitespace only")
            void shouldReturnBadRequestForWhitespaceEmail() throws Exception {
                String whitespaceEmail = "   ";
                ForgetPasswordRequest forgetPasswordRequest = new ForgetPasswordRequest(
                        whitespaceEmail);

                String payload = objectMapper.writeValueAsString(forgetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(FORGET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(tokenRepository.count()).isZero()
                        .as("No token should be created for whitespace email");
            }
        }
    }

    @Nested
    @DisplayName("Change Password")
    class ChangePassword {
        private final String CHANGE_PASSWORD_ENDPOINT = "/password/change-password";

        @Nested
        @DisplayName("Should Change Password Successfully")
        class ShouldChangePasswordSuccessfully {

            @Test
            @DisplayName("Should change password and return 204 when access token is valid")
            void shouldChangePasswordAndReturnNoContentWhenAccessTokenIsValid() throws Exception {
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

                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isNoContent());

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
                String fakeAccessToken = "fake-access-token-99999";
                String newPassword = "NewPass@456";

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                        fakeAccessToken, newPassword);
                String payload = objectMapper.writeValueAsString(changePasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isNotFound());
            }

            @Test
            @DisplayName("Should return 401 Unauthorized when access token is expired")
            void shouldReturnUnauthorizedWhenAccessTokenIsExpired() throws Exception {
                String email = "expired.token@example.com";
                User user = createTestUser(email, "OldPass@123", "Tom", "Davis");

                String accessTokenValue = "expired-access-token";
                Instant expiredDate = Instant.now().minus(1, ChronoUnit.HOURS);
                createAccessToken(user, accessTokenValue, expiredDate);

                String newPassword = "NewPass@456";
                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                        accessTokenValue, newPassword);
                String payload = objectMapper.writeValueAsString(changePasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                User unchangedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches("OldPass@123", unchangedUser.getPassword()))
                        .isTrue().as("Password should remain unchanged");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when password is too short (validation)")
            void shouldReturnBadRequestForInvalidPasswordLength() throws Exception {
                String email = "short.password@example.com";
                User user = createTestUser(email, "OldPass@123", "Sarah", "Miller");

                String accessTokenValue = "valid-access-token";
                Instant expiryDate = Instant.now().plus(30, ChronoUnit.MINUTES);
                createAccessToken(user, accessTokenValue, expiryDate);

                String shortPassword = "Short1";

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                        accessTokenValue, shortPassword);
                String payload = objectMapper.writeValueAsString(changePasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                User unchangedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches("OldPass@123", unchangedUser.getPassword()))
                        .isTrue().as("Password should remain unchanged");
            }
        }
    }

    @Nested
    @DisplayName("Reset Password")
    class ResetPassword {
        private final String RESET_PASSWORD_ENDPOINT = "/password/reset-password";

        @Nested
        @DisplayName("Should Reset Password Successfully")
        class ShouldResetPasswordSuccessfully {

            @Test
            @DisplayName("Should reset password and return 204 when authenticated with valid old password")
            void shouldResetPasswordWhenAuthenticatedWithValidOldPassword() throws Exception {
                String email = "reset.password@example.com";
                String oldPassword = "OldPass@123";
                User user = createTestUser(email, oldPassword, "Emma", "Watson");

                String newPassword = "NewPass@789";
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(oldPassword,
                        newPassword);
                String payload = objectMapper.writeValueAsString(resetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(RESET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(user)))
                        .andExpect(MockMvcResultMatchers.status().isNoContent());

                User updatedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue()
                        .as("Password should be updated to new password");
                assertThat(passwordEncoder.matches(oldPassword, updatedUser.getPassword()))
                        .isFalse().as("Old password should no longer work");
            }
        }

        @Nested
        @DisplayName("Should Fail Resetting Password")
        class ShouldFailResettingPassword {

            @Test
            @DisplayName("Should return 401 Unauthorized when not authenticated")
            void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
                String oldPassword = "OldPass@123";
                String newPassword = "NewPass@789";
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(oldPassword,
                        newPassword);
                String payload = objectMapper.writeValueAsString(resetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(RESET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            }

            @Test
            @DisplayName("Should return 401 unauthorized when old password is incorrect")
            void shouldReturnUnauthorizedWhenOldPasswordIsIncorrect() throws Exception {
                String email = "wrong.old.password@example.com";
                String correctOldPassword = "OldPass@123";
                User user = createTestUser(email, correctOldPassword, "David", "Brown");

                String wrongOldPassword = "WrongPass@999";
                String newPassword = "NewPass@789";
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(
                        wrongOldPassword, newPassword);
                String payload = objectMapper.writeValueAsString(resetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(RESET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(user)))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                User unchangedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches(correctOldPassword, unchangedUser.getPassword()))
                        .isTrue().as("Password should remain unchanged");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when new password is blank (validation)")
            void shouldReturnBadRequestForBlankNewPassword() throws Exception {
                String email = "blank.password@example.com";
                String oldPassword = "OldPass@123";
                User user = createTestUser(email, oldPassword, "Lisa", "Anderson");

                String blankNewPassword = "";
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(oldPassword,
                        blankNewPassword);
                String payload = objectMapper.writeValueAsString(resetPasswordRequest);

                mockMvc.perform(MockMvcRequestBuilders.post(RESET_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(withUserId(user)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                User unchangedUser = userRepository.findByEmail(email).orElseThrow();
                assertThat(passwordEncoder.matches(oldPassword, unchangedUser.getPassword()))
                        .isTrue().as("Password should remain unchanged");
            }
        }
    }
}
