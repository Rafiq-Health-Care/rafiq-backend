package com.nexaworks.rafiq.rabbit.manager;

import static com.nexaworks.rafiq.entities.enums.ActionStatus.CONSULTATION_CANCELLED;
import static com.nexaworks.rafiq.entities.enums.ActionStatus.CONSULTATION_EXPIRED;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;
import com.nexaworks.rafiq.rabbit.publisher.EventPublisher;
import com.nexaworks.rafiq.service.notification.EmailContentService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConsultationNotificationManager {
    public static final String CONSULTATION_CANCELLED_DOCTOR_HTML = "consultation-cancelled-doctor.html";
    public static final String CONSULTATION_CANCELLED_PATIENT_HTML = "consultation-cancelled-patient.html";

    private final EventPublisher<EmailNotification> emailPublisher;
    private final EventPublisher<PushNotification> pushPublisher;
    private final EmailContentService emailContentService;

    public ConsultationNotificationManager(
            @Qualifier("email") EventPublisher<EmailNotification> emailPublisher,
            EventPublisher<PushNotification> pushPublisher,
            EmailContentService emailContentService) {
        this.emailPublisher = emailPublisher;
        this.pushPublisher = pushPublisher;
        this.emailContentService = emailContentService;
    }

    public void sendPatientCancelledEvent(Consultation consultation) {
        Map<String, Object> model = emailContentService.createConsultationCancelledForDoctor(
                consultation.getDoctor().getName(), consultation.getPatient().getName(),
                consultation.getId(), consultation.getCancellationLog().getReason());
        emailPublisher.publish(new EmailNotification(consultation.getDoctor().getEmail(),
                CONSULTATION_CANCELLED_DOCTOR_HTML, "Consultation cancelled", model));
        PushNotification notification = new PushNotification(CONSULTATION_CANCELLED,
                consultation.getPatient().getName() + "cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        pushPublisher.publish(notification);
    }
    public void sendDoctorCancelledEvent(Consultation consultation) {
        Map<String, Object> model = emailContentService.createConsultationCancelledForPatient(
                consultation.getPatient().getName(), consultation.getDoctor().getName(),
                consultation.getId(), consultation.getCancellationLog().getReason());
        emailPublisher.publish(new EmailNotification(consultation.getPatient().getEmail(),
                CONSULTATION_CANCELLED_PATIENT_HTML, "Consultation cancelled", model));
        PushNotification notification = new PushNotification(CONSULTATION_CANCELLED,
                consultation.getDoctor().getName() + "cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        pushPublisher.publish(notification);

    }
    public void publishDoctorExpireNotification(String notificationToken, LocalDateTime startTime) {
        PushNotification notification = new PushNotification(CONSULTATION_EXPIRED,
                notificationToken, Map.of("startTime", startTime.toString()));
        pushPublisher.publish(notification);
    }
}
