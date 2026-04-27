package com.nexaworks.rafiq.eventListener;

import java.io.IOException;
import java.util.Map;

import com.nexaworks.rafiq.dto.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.enums.UploadType;
import com.nexaworks.rafiq.service.doctor.DoctorService;
import com.nexaworks.rafiq.service.file.ImageService;
import com.nexaworks.rafiq.service.notification.EmailContentService;
import com.nexaworks.rafiq.service.notification.EmailSenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {
    public static final String FORGET_PASSWORD_URL = "http://localhost:5173/update-password";
    private final EmailContentService emailContentService;
    private final EmailSenderService emailSenderService;
    private final ImageService imageService;
    private final DoctorService doctorService;

    @Async
    @EventListener
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        emailSenderService.sendEmail(model, event.email(), "Verify your email address",
                "OTP_TEMPLATE.html");
    }

    @Async
    @EventListener
    public void handleNewOtpEvent(NewOtpEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        emailSenderService.sendEmail(model, event.email(), "New OTP", "new-otp.html");
    }

    @Async
    @EventListener
    public void handleForgetPasswordEvent(ForgetPasswordEvent event) {
        Map<String, Object> model = emailContentService
                .createResetPasswordEmail(event.accessToken(), event.name(), FORGET_PASSWORD_URL);
        emailSenderService.sendEmail(model, event.email(), "Reset your password",
                "forget-password.html");
    }
    @Async
    @EventListener
    public void handleDoctorRegistrationEvent(DoctorRegisterEvent event) throws IOException {
        UploadResults uploadResults = imageService.uploadResource(event.nationalId(),
                UploadType.IMAGE);
        log.info("uploaded national ID for doctor with ID: {}", event.doctorId());
        doctorService.updateNationalId(uploadResults, event.doctorId());
        log.info("Updated national ID for doctor with ID: {}", event.doctorId());
        UserRegistrationEvent userRegistrationEvent = event.event();
        Map<String, Object> model = emailContentService.createOtpEmail(userRegistrationEvent.otp(),
                userRegistrationEvent.name(), "url");
        log.info("Sending email to {}", userRegistrationEvent.email());
        emailSenderService.sendEmail(model, userRegistrationEvent.email(),
                "Verify your email address", "OTP_TEMPLATE.html");
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConsultationEvent(ConsultationAddedEvent event) {
        log.info("Received event: {}", event);
        // TODO send email to patient -> will be batch processing
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCancelConsultationEvent(ConsultationCanceled event) {
        try {
            if (event.cancelByPatient()) {
                if (event.doctorEmail() == null || event.doctorEmail().isBlank()) {
                    log.warn("Consultation {} cancelled by patient; doctor email missing, skip notify",
                            event.consultationId());
                    return;
                }
                Map<String, Object> model = emailContentService.createConsultationCancelledForDoctor(
                        event.doctorName(), event.patientName(), event.consultationId(),
                        event.reason());
                emailSenderService.sendEmail(model, event.doctorEmail(),
                        "A patient cancelled a consultation with you",
                        "consultation-cancelled-doctor.html");
                log.info("Sent consultation-cancelled email to doctor for {}", event.consultationId());
            } else {
                if (event.patientEmail() == null || event.patientEmail().isBlank()) {
                    log.warn(
                            "Consultation {} cancelled by doctor; patient email missing, skip notify",
                            event.consultationId());
                    return;
                }
                Map<String, Object> model = emailContentService.createConsultationCancelledForPatient(
                        event.patientName(), event.doctorName(), event.consultationId(),
                        event.reason());
                emailSenderService.sendEmail(model, event.patientEmail(),
                        "Your consultation was cancelled",
                        "consultation-cancelled-patient.html");
                log.info("Sent consultation-cancelled email to patient for {}", event.consultationId());
            }
        } catch (Exception e) {
            log.error("Failed to send consultation cancellation email for {}", event.consultationId(),
                    e);
        }
    }
}
