package com.nexaworks.rafiq.rabbit.manager;

import static com.nexaworks.rafiq.entities.enums.ActionStatus.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;
import com.nexaworks.rafiq.rabbit.publisher.EventPublisher;
import com.nexaworks.rafiq.service.notification.INotificationPersistenceService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConsultationNotificationManager {
    private final EventPublisher<PushNotification> pushPublisher;
    private final INotificationPersistenceService notificationPersistenceService;

    public ConsultationNotificationManager(EventPublisher<PushNotification> pushPublisher,
            INotificationPersistenceService notificationPersistenceService) {

        this.pushPublisher = pushPublisher;
        this.notificationPersistenceService = notificationPersistenceService;
    }

    public void sendPatientCancelledEvent(Consultation consultation) {

        PushNotification notification = PushNotification.of(CONSULTATION_CANCELLED,
                consultation.getDoctor().getNotificationToken(),
                consultation.getPatient().getName() + " cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        notificationPersistenceService.saveNotification(notification, consultation.getPatient());

        pushPublisher.publish(notification);
    }
    public void sendDoctorCancelledEvent(Consultation consultation) {
        PushNotification notification = PushNotification.of(CONSULTATION_CANCELLED,
                consultation.getPatient().getNotificationToken(),
                consultation.getDoctor().getName() + " cancelled the consultation",
                Map.of("consultationId", consultation.getId().toString()));

        notificationPersistenceService.saveNotification(notification, consultation.getDoctor());

        pushPublisher.publish(notification);

    }

    public void publishSuccessfulReservationNotification(Doctor doctor, UUID slotId) {
        PushNotification notification = PushNotification.of(NEW_CONSULTATION,
                doctor.getNotificationToken(), "You have a new consultation",
                Map.of("slotId", slotId.toString()));

        notificationPersistenceService.saveNotification(notification, doctor);

        pushPublisher.publish(notification);
    }
    public void publishFailedReservationNotification(Patient patient, UUID reservationId) {
        PushNotification notification = PushNotification.of(CONSULTATION_FAILED,
                patient.getNotificationToken(), "Your consultation has failed",
                Map.of("slotId", reservationId.toString()));

        notificationPersistenceService.saveNotification(notification, patient);

        pushPublisher.publish(notification);
    }

    public void publishReminderNotification(Consultation consultation, LocalDateTime startTime) {
        UUID consultationId = consultation.getId();
        PushNotification notification = PushNotification.of(CONSULTATION_COMING_UP,
                consultation.getPatient().getNotificationToken(), "Your consultation is coming up",
                Map.of("startTime", startTime.toString(), "consultationId",
                        consultationId.toString()));
        log.info("Publishing reminder notification for consultation: {} at: {}", consultationId,
                startTime);
        PushNotification notification2 = PushNotification.of(CONSULTATION_COMING_UP,
                consultation.getDoctor().getNotificationToken(), "Your consultation is coming up",
                Map.of("startTime", startTime.toString(), "consultationId",
                        consultationId.toString()));
        log.info("Publishing reminder notification for consultation: {} at: {}", consultationId,
                startTime);
        notificationPersistenceService.saveNotification(notification2, consultation.getDoctor());

        notificationPersistenceService.saveNotification(notification, consultation.getPatient());

        pushPublisher.publish(notification);
        pushPublisher.publish(notification2);
    }
}
