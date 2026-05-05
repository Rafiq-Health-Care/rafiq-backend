package com.nexaworks.rafiq.service.user;

import java.time.Instant;
import java.util.Optional;

import com.nexaworks.rafiq.config.RabbitMQConfig;
import com.nexaworks.rafiq.dto.notificaiton.EmailNotification;
import com.nexaworks.rafiq.service.notification.EmailContentService;
import com.nexaworks.rafiq.service.rabbit.MessageService;
import org.springframework.amqp.core.AmqpTemplate;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;



    @Override
    @Transactional
    public void forgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        String email = forgetPasswordRequest.email();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }
        String token = tokenService.generateAccessToken(user);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messageService.sendResetPasswordEvent(user.get(),token);
                    }
                }
        );
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
