package com.nexaworks.rafiq.notification.listener;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.notification.service.EmailContentService;
import com.nexaworks.rafiq.notification.service.EmailSenderService;
import com.nexaworks.rafiq.shared.event.doctor.DoctorRegisterEvent;
import com.nexaworks.rafiq.shared.event.patient.PatientRegistrationEvent;
import com.nexaworks.rafiq.shared.event.user.ForgetPasswordEvent;
import com.nexaworks.rafiq.shared.event.user.NewOtpEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {
    public static final String FORGET_PASSWORD_URL = "http://localhost:5173/update-password";
    public static final String USER_REGISTRATION_EMAIL_TEMPLATE = "OTP_TEMPLATE.html";
    public static final String SUBJECT_USER_REGISTRATION = "Verify your email address";
    public static final String NEW_OTP_TEMPLATE = "new-otp.html";
    public static final String NEW_OTP_EMAIL_SUBJECT = "New OTP";
    public static final String FORGET_PASSWORD_EMAIL_TEMPLATE = "forget-password.html";
    public static final String RESET_PASSWORD_EMAIL_SUBJECT = "Reset your password";
    public static final String EMAIL_TEMPLATE_DOCTOR_REGISTER = "doctor-register.html";
    private final EmailSenderService emailSenderService;
    private final EmailContentService emailContentService;

    @Async
    @EventListener
    public void handlePatientRegistrationEvent(PatientRegistrationEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(),
                event.firstName(), "url");
        emailSenderService.sendEmail(model, event.email(), SUBJECT_USER_REGISTRATION,
                USER_REGISTRATION_EMAIL_TEMPLATE);
    }

    @Async
    @EventListener
    public void handleNewOtpEvent(NewOtpEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        emailSenderService.sendEmail(model, event.email(), NEW_OTP_EMAIL_SUBJECT, NEW_OTP_TEMPLATE);
    }
    @Async
    @EventListener
    public void handleForgetPasswordEvent(ForgetPasswordEvent event) {
        Map<String, Object> model = emailContentService
                .createResetPasswordEmail(event.accessToken(), event.name(), FORGET_PASSWORD_URL);
        emailSenderService.sendEmail(model, event.email(), RESET_PASSWORD_EMAIL_SUBJECT,
                FORGET_PASSWORD_EMAIL_TEMPLATE);
    }
    @Async
    @EventListener
    public void handleDoctorRegistrationEvent(DoctorRegisterEvent event) throws IOException {

        PatientRegistrationEvent patientRegistrationEvent = event.basicInfo();
        Map<String, Object> model = emailContentService.createOtpEmail(
                patientRegistrationEvent.otp(), patientRegistrationEvent.firstName(), "url");
        emailSenderService.sendEmail(model, patientRegistrationEvent.email(),
                SUBJECT_USER_REGISTRATION, EMAIL_TEMPLATE_DOCTOR_REGISTER);
    }
}
