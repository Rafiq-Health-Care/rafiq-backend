package com.nexaworks.rafiq.service.consultation;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.constant.CacheNames;
import com.nexaworks.rafiq.dto.request.consultation.ReserveConsultationRequest;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationEvent;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.EventType;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationOverlappingException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotReservedException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.payment.PaymentService;
import com.stripe.exception.StripeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService implements IReservationService {
    private final ConsultationRepository consultationRepository;
    private final ConsultationSlotRepository slotRepository;
    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PaymentService paymentService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DOCTOR_AVAILABLE_SLOTS, allEntries = true)
    @Retryable(retryFor = {
            PessimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public String reserve(ReserveConsultationRequest request) {
        Patient patient = (Patient) authService.getAuthenticateUser();
        log.info("Patient {} is reserving consultation {}", patient.getEmail(), request.slotId());

        ConsultationSlot slot = slotRepository.findConsultationByIdWithLock(request.slotId())
                .orElseThrow(() -> new SlotNotFoundException("Consultation not found"));

        if (!slot.getStatus().equals(SlotStatus.AVAILABLE)) {
            throw new SlotReservedException("Consultation cannot be reserved");
        }

        checkPatientOverlapping(slot.getStartTime(), slot.getEndTime(), patient);
        Consultation consultation = Consultation.builder().slot(slot).patient(patient)
                .doctor(slot.getDoctor()).notes(request.notes()).build();

        log.info("Consultation {} is reserved by {}", slot.getId(), patient.getId());

        try {
            String clientSecret = paymentService.process(consultation, patient, request.provider());
            slot.setStatus(SlotStatus.PENDING_PAYMENT);

            consultationRepository.save(consultation);

            log.debug("Payment key for consultation {} is {}", consultation.getId(), clientSecret);

            TransactionSynchronizationManager
                    .registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            messagingTemplate.convertAndSend("/topic/consultation",
                                    new ConsultationEvent(consultation.getId(), EventType.BOOKED,
                                            Map.of()));
                        }
                    });

            return clientSecret;
        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void checkPatientOverlapping(LocalDateTime start, LocalDateTime end,
            Patient currentUser) {
        if (consultationRepository.existsByPatientOverlapping(start, end, currentUser.getId())) {
            throw new ConsultationOverlappingException("Consultation time slot is already booked");
        }
    }
}
