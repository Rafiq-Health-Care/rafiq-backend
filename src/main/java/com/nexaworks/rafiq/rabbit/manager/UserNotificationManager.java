package com.nexaworks.rafiq.rabbit.manager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.notificaiton.EmailNotification;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.rabbit.publisher.EventPublisher;
import com.nexaworks.rafiq.service.notification.EmailContentService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserNotificationManager {
    public static final String OTP_NOTIFICATION_TEMPLATE = "new-otp.html";
    public static final String DEFAULT_URL = "";
    private static final String RESET_PASSWORD_URL = "http://localhost:8080/api/v1/auth/reset-password";
    private static final String EMAIL_TEMPLATE = "forget-password.html";

    private final EmailContentService emailContentService;
    private final EventPublisher<EmailNotification> otpEmailPublisher;
    private final EventPublisher<EmailNotification> emailPublisher;

    public UserNotificationManager(EmailContentService emailContentService,
            @Qualifier("otpEmail") EventPublisher<EmailNotification> otpEmailPublisher,
            @Qualifier("email") EventPublisher<EmailNotification> emailPublisher) {
        this.emailContentService = emailContentService;
        this.otpEmailPublisher = otpEmailPublisher;
        this.emailPublisher = emailPublisher;
    }

    public void sendResetPasswordEvent(User user, String token) {
        emailPublisher.publish(new EmailNotification(user.getEmail(), EMAIL_TEMPLATE,
                "Reset Password", emailContentService.createResetPasswordEmail(token,
                        user.getFirstName(), RESET_PASSWORD_URL)));
    }

    public void sendNewOtpEvent(User user, String otp) {
        otpEmailPublisher.publish(
                new EmailNotification(user.getEmail(), OTP_NOTIFICATION_TEMPLATE, "New OTP",
                        emailContentService.createOtpEmail(otp, user.getFirstName(), DEFAULT_URL)));
    }
    public void sendRegistrationEvent(User user, String otp) {
        otpEmailPublisher.publish(new EmailNotification(user.getEmail(), OTP_NOTIFICATION_TEMPLATE,
                "Verify your email address",
                emailContentService.createOtpEmail(otp, user.getFirstName(), DEFAULT_URL)));
    }
}
