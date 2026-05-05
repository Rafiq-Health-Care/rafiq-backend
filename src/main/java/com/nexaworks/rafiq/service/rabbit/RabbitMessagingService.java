package com.nexaworks.rafiq.service.rabbit;

import com.nexaworks.rafiq.config.RabbitMQConfig;
import com.nexaworks.rafiq.dto.notificaiton.EmailNotification;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.service.notification.EmailContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.nexaworks.rafiq.config.RabbitMQConfig.*;


@Service
@Qualifier("rabbit")
@RequiredArgsConstructor
@Slf4j
public class RabbitMessagingService implements MessageService{
    public static final String OTP_NOTIFICATION_TEMPLATE = "new-otp.html";
    public static final String DEFAULT_URL = "";
    private final AmqpTemplate rabbitTemplate;
    private final EmailContentService emailContentService;
    private static final String RESET_PASSWORD_URL = "http://localhost:8080/api/v1/auth/reset-password";
    private static final String EMAIL_TEMPLATE = "forget-password.html";

    public void sendResetPasswordEvent(User user, String token) {
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE,
               ROUTING_KEY_EMAIL,
                new EmailNotification(user.getEmail(),
                        EMAIL_TEMPLATE,"Reset Password",
                        emailContentService.createResetPasswordEmail(token, user.getFirstName(),
                                RESET_PASSWORD_URL)));
    }

    public void sendNewOtpEvent(User user, String otp) {
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE,
                ROUTING_KEY_EMAIL,
                new EmailNotification(user.getEmail(), OTP_NOTIFICATION_TEMPLATE,"New OTP"
                        ,emailContentService.createOtpEmail(otp, user.getFirstName(), DEFAULT_URL)));
    }
    public void sendRegistrationEvent(User user, String otp) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_EMAIL,
                new EmailNotification(user.getEmail(),OTP_NOTIFICATION_TEMPLATE,
                        "Verify your email address"
                        ,emailContentService.createOtpEmail(otp, user.getFirstName(),DEFAULT_URL)));
    }
}
