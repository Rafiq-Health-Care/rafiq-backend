package com.nexaworks.rafiq.user.service.implementation;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.shared.event.user.ForgetPasswordEvent;
import com.nexaworks.rafiq.user.api.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ResetPasswordRequest;
import com.nexaworks.rafiq.user.entity.model.Token;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.exception.TokenInvalidException;
import com.nexaworks.rafiq.user.exception.UserNotFoundException;
import com.nexaworks.rafiq.user.repository.UserRepository;
import com.nexaworks.rafiq.user.service.PasswordService;
import com.nexaworks.rafiq.user.service.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;
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
    public void resetPassword(ResetPasswordRequest resetPasswordRequest, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: "));
        if (passwordEncoder.matches(resetPasswordRequest.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
        }
        userRepository.save(user);
    }

}
