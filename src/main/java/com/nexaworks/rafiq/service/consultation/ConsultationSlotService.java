package com.nexaworks.rafiq.service.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.event.ConsultationChanged;
import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.EditConsultationSlotRequest;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.auth.AuthorizationException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotCanNotCreated;
import com.nexaworks.rafiq.exception.custom.consultation.SlotCanNotEditException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.scheduler.ExpirationScheduler;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.utils.TransactionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationSlotService implements IConsultationSlotService {
    private final ConsultationSlotRepository consultationSlotRepository;
    private final AuthService authService;
    private final DoctorRepository doctorRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionUtils transactionUtils;
    private final ExpirationScheduler scheduler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(retryFor = {
            PessimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public ConsultationSlot add(AddConsultationRequest request) {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();

        doctorRepository.findByIdWithLock(doctor.getId());

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = request.startTime().plusMinutes(request.duration());

        if (consultationSlotRepository.existsByOverlapping(startTime, endTime, doctor.getId())) {
            throw new SlotCanNotCreated(
                    "Can't create consultation slot, you have overlapping consultation slot");
        }

        ConsultationSlot slot = ConsultationSlot.builder().doctor(doctor).startTime(startTime)
                .endTime(endTime).durationMinutes(request.duration()).build();

        slot = consultationSlotRepository.save(slot);
        log.info("Slot added {} by {}", slot.getId(), doctor.getId());
        scheduler.scheduleConsultationSlotExpiration(slot.getId(), slot.getEndTime());
        return slot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(retryFor = {
            PessimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public ConsultationSlot editConsultation(EditConsultationSlotRequest request, UUID id) {
        UUID userId = authService.getAuthenticateUserId();

        ConsultationSlot slot = consultationSlotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException("Consultation slot not found"));

        validateEditability(slot, userId);

        doctorRepository.findByIdWithLock(userId);

        LocalDateTime start = request.startTime();
        LocalDateTime end = request.startTime().plusMinutes(request.duration());
        if (consultationSlotRepository.existsByOverlapping(start, end, userId, id,
                SlotStatus.CANCELLED)) {
            throw new SlotCanNotEditException("Consultation time slot is already booked");
        }
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setDurationMinutes(request.duration());
        slot.setStatus(request.status());
        log.info("Slot edited {} by {}", slot.getId(), userId);

        // todo migrate to server send events
        transactionUtils.afterCommit(() -> messagingTemplate.convertAndSend("/topic/consultation",
                new ConsultationChanged(slot.getId(), start)));
        scheduler.reSchedule(slot.getId(), slot.getEndTime());

        return slot;
    }

    private static void validateEditability(ConsultationSlot slot, UUID userId) {
        if (slot.getStatus() == SlotStatus.EXPIRED) {
            throw new SlotCanNotEditException("Consultation is already completed");
        }
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new SlotCanNotEditException("You cannot edit on booked consultation");
        }
        if (!slot.getDoctor().getId().equals(userId)) {
            throw new AuthorizationException("You are not authorized to edit this consultation");
        }
    }

}
