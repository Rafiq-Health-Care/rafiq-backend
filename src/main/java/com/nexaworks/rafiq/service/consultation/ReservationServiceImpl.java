package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.response.consultation.ConsultationEvent;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.EventType;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.exception.custom.ConsultationException;
import com.nexaworks.rafiq.repository.CancellationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService{
    private final ConsultationRepository consultationRepository;
    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PaymentService paymentService;


    @Override
    @Transactional
    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public String reserve(UUID id, PaymentProvider provider) {
        Patient patient = (Patient) authService.getAuthenticateUser();
        log.info("Patient {} is reserving consultation {}", patient.getEmail(), id);
        Consultation consultation = consultationRepository.findConsultationById(id)
                .orElseThrow(()->new ConsultationException("Consultation not found"));

        if (!consultation.getStatus().equals(ConsultationStatus.AVAILABLE)){
            throw new ConsultationException("Consultation cannot be reserved");
        }

        checkPatientOverlapping(consultation, patient);
        consultation.setStatus(ConsultationStatus.BOOKED);

        log.info("Consultation {} is reserved by {}", consultation.getId(), patient.getEmail());

        consultation.setPatient(patient);

        String clientSecret = paymentService.process(consultation,patient,provider);

        consultationRepository.save(consultation);

        log.debug("Payment key for consultation {} is {}", consultation.getId(), clientSecret);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit(){
                        // TODO add expire date to the consultation
                        messagingTemplate.convertAndSend("/topic/consultation",
                                new ConsultationEvent(consultation.getId(), EventType.BOOKED, Map.of()));
                    }
                }
        );

        return clientSecret;
    }

    private void checkPatientOverlapping(Consultation consultation, Patient currentUser) {
        if (consultationRepository.existsByPatientOverlapping(consultation.getTimeSlot().getStartTime(),
                consultation.getTimeSlot().getEndTime(),currentUser.getId(),ConsultationStatus.CANCELLED)){
            throw new ConsultationException("Consultation time slot is already booked");
        }
    }
}
