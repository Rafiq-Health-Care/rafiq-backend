package com.nexaworks.rafiq.service.consultation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.exception.custom.ConsultationNotFoundException;
import com.nexaworks.rafiq.exception.custom.RtcProviderException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.call.RtcProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationPreparationService implements IConsultationPreparationService {
    private final ConsultationRepository consultationRepository;
    private final RtcProvider rtcProvider;

    @Override
    @Transactional(rollbackOn = {RtcProviderException.class})
    public String prepare(UUID uuid) {
        Consultation consultation = consultationRepository.findConsultationById(uuid)
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));

        if (!consultation.getStatus().isPreparable()) {
            return null;
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
        return accessToken;
    }
}
