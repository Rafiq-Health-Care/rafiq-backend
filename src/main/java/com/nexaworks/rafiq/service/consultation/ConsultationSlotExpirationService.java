package com.nexaworks.rafiq.service.consultation;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultationSlotExpirationService implements IConsultationSlotExpirationService {
    private final ConsultationSlotRepository consultationSlotRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void expire(UUID consultationSlotId) {
        log.info("Expiring consultation slot: {}", consultationSlotId);
        ConsultationSlot consultationSlot = consultationSlotRepository.findById(consultationSlotId)
                .orElseThrow(() -> new SlotNotFoundException("Consultation slot not found"));
        if (consultationSlot.getEndTime().isAfter(LocalDateTime.now())) {
            return;
        }
        if (consultationSlot.getStatus().equals(SlotStatus.BOOKED)) {
            messagingTemplate.convertAndSend("/queue/" + consultationSlotId, Optional.of(1));
            consultationSlot.getConsultation().setStatus(ConsultationStatus.COMPLETED);
        }
        consultationSlot.setStatus(SlotStatus.EXPIRED);
        consultationSlotRepository.save(consultationSlot);
    }
}
