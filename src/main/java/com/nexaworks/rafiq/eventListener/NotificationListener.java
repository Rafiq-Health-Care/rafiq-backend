package com.nexaworks.rafiq.eventListener;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.dto.event.DoctorRegisterEvent;
import com.nexaworks.rafiq.dto.event.ForgetPasswordEvent;
import com.nexaworks.rafiq.dto.event.NewOtpEvent;
import com.nexaworks.rafiq.dto.event.UserRegistrationEvent;
import com.nexaworks.rafiq.enums.UploadType;
import com.nexaworks.rafiq.service.DoctorService;
import com.nexaworks.rafiq.service.EmailContentService;
import com.nexaworks.rafiq.service.EmailSenderService;
import com.nexaworks.rafiq.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {
    private final EmailContentService emailContentService;
    private final EmailSenderService emailSenderService;
    private final ImageService imageService;
    private final DoctorService doctorService;

    @Async
    @EventListener
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "Verify your email address",
                "OTP_TEMPLATE.html");
    }

    @Async
    @EventListener
    public void handleNewOtpEvent(NewOtpEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "New OTP", "new-otp.html");
    }

    @Async
    @EventListener
    public void handleForgetPasswordEvent(ForgetPasswordEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        log.info("Sending email to {}", event.email());
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
}
