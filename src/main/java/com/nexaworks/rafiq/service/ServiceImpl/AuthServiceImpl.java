package com.nexaworks.rafiq.service.ServiceImpl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nexaworks.rafiq.dto.event.ForgetPasswordEvent;
import com.nexaworks.rafiq.dto.request.user.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.VerifyOtpRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.dto.response.auth.VerifyOtpResponse;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.GoogleAuthException;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.*;
import com.nexaworks.rafiq.utils.AuthSessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthSessionManager authSessionManager;
    private final GoogleIdTokenVerifier verifier;

    @Override
    @Transactional
    public void forgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        String email = forgetPasswordRequest.email();
        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }
        String token = tokenService.generateAccessToken(user);
        log.info("Generated OTP {}", token);
        eventPublisher
                .publishEvent(new ForgetPasswordEvent(email, token, user.get().getFirstName()));
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        validateToken(verifyOtpRequest);
        String accessToken = tokenService
                .generateAccessToken(userService.findByEmail(verifyOtpRequest.email()));
        return new VerifyOtpResponse(accessToken);
    }

    private void validateToken(VerifyOtpRequest verifyOtpRequest) {
        Token otp = tokenService.getToken(verifyOtpRequest.otp());
        if (!otp.getUser().getEmail().equals(verifyOtpRequest.email())
                || otp.getExpiryDate().isBefore(Instant.now())) {
            log.error(otp.getUser().getEmail() + " " + verifyOtpRequest.email());
            throw new TokenInvalidException("Invalid OTP");
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Token token = tokenService.getToken(changePasswordRequest.accessToken());
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenInvalidException("Invalid Access Token");
        }
        User user = token.getUser();
        userService.changePassword(user, changePasswordRequest.newPassword());
        log.info("Password changed for user {}", user.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = getAuthenticateUser();
        userService.updatePassword(user, resetPasswordRequest);
    }

    public User getAuthenticateUser() {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userId).orElseThrow();
    }

    @Override
    @Transactional
    public LoginResponse login(String email, String password, HttpServletResponse response) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = (User) authentication.getPrincipal();
        return authSessionManager.createLoginSession(response, user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(HttpServletResponse response, HttpServletRequest request) {
        Token token = tokenService.getToken(authSessionManager.getCookie(request, "refreshToken"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenInvalidException("Invalid Refresh Token");
        }
        User user = token.getUser();
        return authSessionManager.createLoginSession(response, user);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authSessionManager.getCookie(request, "refreshToken");
        Token token = tokenService.getToken(refreshToken);
        tokenService.invalidateRefreshToken(token);
        String jwt = authSessionManager.getCookie(request, "jwt");
        if (jwt != null) {
            jwtService.invalidateJwtToken(jwt);
        }
        authSessionManager.invalidateSession(response);
    }

    @Override
    @Transactional
    public LoginResponse oAuth2(String idToken, HttpServletResponse response) {
        GoogleIdToken googleIdToken = getGoogleIdToken(idToken);
        String email = googleIdToken.getPayload().getEmail();
        String firstName = googleIdToken.getPayload().get("given_name").toString();
        String lastName = googleIdToken.getPayload().get("family_name").toString();
        Optional<User> user = getUser(email, firstName, lastName);
        if (user.isPresent()) {
            return authSessionManager.createLoginSession(response, user.get());
        } else {
            throw new GoogleAuthException("Failed to authenticate user with Google");
        }
    }

    @NotNull
    private Optional<User> getUser(String email, String firstName, String lastName) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent()) {
            User existingUser = user.get();
            if (!existingUser.isEnabled()) {
                existingUser.setEnabled(true);
                userRepository.save(existingUser);
            }
        } else {
            user = Optional.ofNullable(userService.addUser(email, firstName, lastName));
        }
        return user;
    }

    private GoogleIdToken getGoogleIdToken(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new GoogleAuthException("Invalid id token");
            }
            return token;
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error verifying Google ID token", e);
            throw new GoogleAuthException("Failed to verify Google ID token");
        }
    }

}
