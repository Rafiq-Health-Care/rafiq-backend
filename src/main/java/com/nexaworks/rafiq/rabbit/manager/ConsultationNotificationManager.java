package com.nexaworks.rafiq.rabbit.manager;

import static com.nexaworks.rafiq.entities.enums.ActionStatus.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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
        emailPublisher.publish(EmailNotification.of(consultation.getDoctor().getEmail(),
                CONSULTATION_CANCELLED_DOCTOR_HTML, "Consultation cancelled", model));
        PushNotification notification = PushNotification.of(CONSULTATION_CANCELLED,
                consultation.getDoctor().getNotificationToken(),
                consultation.getPatient().getName() + " cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        pushPublisher.publish(notification);
    }
    public void sendDoctorCancelledEvent(Consultation consultation) {
        Map<String, Object> model = emailContentService.createConsultationCancelledForPatient(
                consultation.getPatient().getName(), consultation.getDoctor().getName(),
                consultation.getId(), consultation.getCancellationLog().getReason());
        emailPublisher.publish(EmailNotification.of(consultation.getPatient().getEmail(),
                CONSULTATION_CANCELLED_PATIENT_HTML, "Consultation cancelled", model));
        PushNotification notification = PushNotification.of(CONSULTATION_CANCELLED,
                consultation.getPatient().getNotificationToken(),
                consultation.getDoctor().getName() + " cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        pushPublisher.publish(notification);

    }

    public void publishSuccessfulReservationNotification(String notificationToken, UUID slotId) {
        PushNotification notification = PushNotification.of(NEW_CONSULTATION, notificationToken,
                "You have a new consultation", Map.of("slotId", slotId.toString()));
        pushPublisher.publish(notification);
    }
    public void publishFailedReservationNotification(String notificationToken, UUID reservationId) {
        PushNotification notification = PushNotification.of(CONSULTATION_FAILED, notificationToken,
                "Your consultation has failed", Map.of("slotId", reservationId.toString()));
        pushPublisher.publish(notification);
    }

    public void publishReminderNotification(UUID consultationId, String fcm,
            LocalDateTime startTime) {
        PushNotification notification = PushNotification.of(CONSULTATION_COMING_UP, fcm,
                "Your consultation is coming up", Map.of("startTime", startTime.toString(),
                        "consultationId", consultationId.toString()));
        pushPublisher.publish(notification);
    }
}
