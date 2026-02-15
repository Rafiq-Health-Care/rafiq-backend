package com.nexaworks.rafiq.service.user;

import java.time.Instant;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.event.ForgetPasswordEvent;
import com.nexaworks.rafiq.dto.request.user.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void forgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        String email = forgetPasswordRequest.email();
        Optional<User> user = userRepository.findByEmail(email);
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
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Token token = tokenService.getToken(changePasswordRequest.accessToken());
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenInvalidException("Invalid Access Token");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
        log.info("Password changed for user {}", user.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = authService.getAuthenticateUser();
        if (passwordEncoder.matches(resetPasswordRequest.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
        }
        userRepository.save(user);
    }

}
