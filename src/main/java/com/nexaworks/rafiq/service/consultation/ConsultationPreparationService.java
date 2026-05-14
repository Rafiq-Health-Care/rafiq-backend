package com.nexaworks.rafiq.service.consultation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.notificaiton.PushNotification;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.ActionStatus;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.exception.custom.ConsultationNotFoundException;
import com.nexaworks.rafiq.exception.custom.RtcProviderException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.call.RtcProvider;
import com.nexaworks.rafiq.service.notification.NotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationPreparationService implements IConsultationPreparationService {
    private final NotificationService<PushNotification> notificationService;
    private final ConsultationRepository consultationRepository;
    private final RtcProvider rtcProvider;

    @Override
    @Transactional(rollbackOn = {RtcProviderException.class})
    public void prepare(UUID uuid) {
        Consultation consultation = consultationRepository.findConsultationById(uuid)
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));

        if (!consultation.getStatus().isPreparable()) {
            return;
        }
        int expiration = Math.toIntExact(LocalDateTime.now()
                .until(consultation.getTimeSlot().getEndTime(), ChronoUnit.SECONDS));

        String accessToken = rtcProvider.generateToken(consultation.getId().toString(), expiration);

        if (accessToken == null) {
            log.error("Failed to generate access token for consultation {}", consultation.getId());
            throw new RtcProviderException("Failed to generate access token for consultation");
        }

        consultation.setStatus(ConsultationStatus.LIVE);
        consultation.setAccessToken(accessToken);
        consultationRepository.save(consultation);

        sendNotificationsToPatientAndDoctor(consultation, accessToken);
    }

    private void sendNotificationsToPatientAndDoctor(Consultation consultation,
            String accessToken) {
        Map<String, Object> data = new HashMap<>();
        Doctor doctor = consultation.getDoctor();
        Patient patient = consultation.getPatient();
        data.put("doctorName", doctor.getName());
        data.put("patientName", patient.getName());
        data.put("token", accessToken);
        data.put("consultationId", consultation.getId().toString());
        PushNotification patientNotification = new PushNotification(
                ActionStatus.CONSULTATION_COMING_UP, patient.getNotificationToken(), data);
        notificationService.sendNotification(patientNotification);
        PushNotification doctorNotification = new PushNotification(
                ActionStatus.CONSULTATION_COMING_UP, doctor.getNotificationToken(), data);
        notificationService.sendNotification(doctorNotification);
    }
}
