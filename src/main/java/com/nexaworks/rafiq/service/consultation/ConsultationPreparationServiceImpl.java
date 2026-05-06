package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.notificaiton.PushNotification;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.exception.custom.ConsultationException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.call.RtcProvider;
import com.nexaworks.rafiq.service.notification.NotificationService;
import io.micrometer.core.aop.CountedAspect;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationPreparationServiceImpl implements ConsultationPreparationService{
    private final NotificationService<PushNotification> notificationService;
    private final ConsultationRepository consultationRepository;
    private final RtcProvider rtcProvider;


    @Override
    @Transactional
    public void prepare(UUID uuid) {
        Consultation consultation = consultationRepository.findConsultationById(uuid).orElseThrow(()->
                new ConsultationException("Consultation not found"));

        if (!consultation.getStatus().isPreparable()){
            return;
        }
        int expiration = Math.toIntExact(LocalDateTime.now().until(consultation.getTimeSlot().getEndTime(), ChronoUnit.SECONDS));

        String accessToken = rtcProvider.generateToken(consultation.getId().toString(),expiration);

        if (accessToken == null){
            log.error("Failed to generate access token for consultation {}",consultation.getId());
            // TODO Throw exception or handle accordingly
        }

        consultation.setStatus(ConsultationStatus.LIVE);

        //TODO send push notification to patient and doctor



    }
}
