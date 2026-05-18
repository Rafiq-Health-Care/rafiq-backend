package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.ConsultationNotFoundException;
import com.nexaworks.rafiq.rabbit.manager.ConsultationNotificationManager;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.scheduler.PreparationScheduler;
import com.nexaworks.rafiq.utils.TransactionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultationProcessingService implements IConsultationProcessingService {
    private final ConsultationRepository consultationRepository;
    private final ConsultationNotificationManager notificationManager;
    private final TransactionUtils transactionUtils;
    private final PreparationScheduler preparationScheduler;

    @Override
    @Transactional
    public void success(UUID id) {
        Consultation consultation = consultationRepository.findById(id).orElseThrow(
                () -> new ConsultationNotFoundException("Consultation not found with id: " + id));
        log.info("Consultation with id: {} is confirmed", id);

        consultation.setStatus(ConsultationStatus.CONFIRMED);
        consultation.getSlot().setStatus(SlotStatus.BOOKED);

        consultationRepository.save(consultation);
        transactionUtils.afterCommit(() -> {
            log.info("Consultation with id: {} is confirmed", id);
            preparationScheduler.scheduleReminder(id,
                    consultation.getPatient().getNotificationToken(),
                    consultation.getSlot().getStartTime());
            notificationManager.publishSuccessfulReservationNotification(
                    consultation.getSlot().getDoctor().getNotificationToken(),
                    consultation.getSlot().getId());
        });

    }

    @Override
    @Transactional
    public void failed(UUID id) {
        Consultation consultation = consultationRepository.findById(id).orElseThrow(
                () -> new ConsultationNotFoundException("Consultation not found with id: " + id));

        consultation.setStatus(ConsultationStatus.CANCELLED);
        consultation.getSlot().setStatus(SlotStatus.AVAILABLE);

        consultationRepository.save(consultation);
        transactionUtils.afterCommit(() -> {
            log.info("Consultation with id: {} is cancelled", id);
            notificationManager.publishFailedReservationNotification(
                    consultation.getPatient().getNotificationToken(), consultation.getId());
        });

    }
}
