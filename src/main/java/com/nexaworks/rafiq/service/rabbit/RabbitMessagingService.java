package com.nexaworks.rafiq.service.rabbit;

import static com.nexaworks.rafiq.constant.RabbitMQConstant.*;
import static com.nexaworks.rafiq.entities.enums.ActionStatus.CONSULTATION_CANCELLED;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.notificaiton.EmailNotification;
import com.nexaworks.rafiq.dto.notificaiton.PushNotification;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.service.notification.EmailContentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("rabbit")
@RequiredArgsConstructor
@Slf4j
public class RabbitMessagingService implements MessageService {
    public static final String OTP_NOTIFICATION_TEMPLATE = "new-otp.html";
    public static final String DEFAULT_URL = "";
    public static final String CONSULTATION_CANCELLED_DOCTOR_HTML = "consultation-cancelled-doctor.html";
    public static final String CONSULTATION_CANCELLED_PATIENT_HTML = "consultation-cancelled-patient.html";
    private final AmqpTemplate rabbitTemplate;
    private final EmailContentService emailContentService;
    private static final String RESET_PASSWORD_URL = "http://localhost:8080/api/v1/auth/reset-password";
    private static final String EMAIL_TEMPLATE = "forget-password.html";

    public void sendResetPasswordEvent(User user, String token) {
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_EMAIL,
                new EmailNotification(user.getEmail(), EMAIL_TEMPLATE, "Reset Password",
                        emailContentService.createResetPasswordEmail(token, user.getFirstName(),
                                RESET_PASSWORD_URL)));
    }

    public void sendNewOtpEvent(User user, String otp) {
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_EMAIL,
                new EmailNotification(user.getEmail(), OTP_NOTIFICATION_TEMPLATE, "New OTP",
                        emailContentService.createOtpEmail(otp, user.getFirstName(), DEFAULT_URL)));
    }
    public void sendRegistrationEvent(User user, String otp) {
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_EMAIL,
                new EmailNotification(user.getEmail(), OTP_NOTIFICATION_TEMPLATE,
                        "Verify your email address",
                        emailContentService.createOtpEmail(otp, user.getFirstName(), DEFAULT_URL)));
    }

    @Override
    public void sendPatientCancelledEvent(Consultation consultation) {
        Map<String, Object> model = emailContentService.createConsultationCancelledForDoctor(
                consultation.getDoctor().getName(), consultation.getPatient().getName(),
                consultation.getId(), consultation.getCancellationLog().getReason());
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_EMAIL,
                new EmailNotification(consultation.getDoctor().getEmail(),
                        CONSULTATION_CANCELLED_DOCTOR_HTML, "Consultation cancelled", model));
        PushNotification notification = new PushNotification(CONSULTATION_CANCELLED,
                consultation.getPatient().getName() + "cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_PUSH, notification);
    }

    @Override
    public void sendDoctorCancelledEvent(Consultation consultation) {
        Map<String, Object> model = emailContentService.createConsultationCancelledForPatient(
                consultation.getPatient().getName(), consultation.getDoctor().getName(),
                consultation.getId(), consultation.getCancellationLog().getReason());
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_EMAIL,
                new EmailNotification(consultation.getPatient().getEmail(),
                        CONSULTATION_CANCELLED_PATIENT_HTML, "Consultation cancelled", model));
        PushNotification notification = new PushNotification(CONSULTATION_CANCELLED,
                consultation.getDoctor().getName() + "cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ROUTING_KEY_PUSH, notification);

    }

    @Override
    public void publishExpirationEvent(UUID id, LocalDateTime endTime) {
        long delay = Math.max(0, LocalDateTime.now().until(endTime, ChronoUnit.MILLIS));
        rabbitTemplate.convertAndSend(CONSULTATION_EXPIRATION_EXCHANGE,
                CONSULTATION_EXPIRATION_ROUTING_KEY, id.toString(), message -> {
                    message.getMessageProperties().setHeader("x-delay", delay);
                    return message;
                });
    }

    @Override
    public void publishPreparationEvent(UUID id, LocalDateTime startTime) {
        long delay = Math.max(0, LocalDateTime.now().until(startTime, ChronoUnit.MILLIS));

        rabbitTemplate.convertAndSend(CONSULTATION_PREPARATION_EXCHANGE,
                CONSULTATION_PREPARATION_ROUTING_KEY, id.toString(), message -> {
                    message.getMessageProperties().setHeader("x-delay", delay);
                    return message;
                });
    }

}
