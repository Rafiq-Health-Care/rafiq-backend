package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.event.ConsultationAddedEvent;
import com.nexaworks.rafiq.dto.event.ConsultationCanceled;
import com.nexaworks.rafiq.dto.event.ConsultationCancelled;
import com.nexaworks.rafiq.dto.event.ConsultationChanged;
import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationEvent;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.EventType;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.exception.custom.ConsultationException;
import com.nexaworks.rafiq.mapper.ConsultationMapper;
import com.nexaworks.rafiq.repository.CancellationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.repository.specification.ScheduleSpecification;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService{
    private final ConsultationRepository consultationRepository;
    private final CancellationLogRepository cancellationLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthService authService;
    private final DoctorRepository doctorRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PaymentService paymentService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public Consultation add(Consultation entity) {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();
        entity.setDoctor(doctor);

        doctorRepository.findByIdWithLock(doctor.getId());

        LocalDateTime startTime = entity.getTimeSlot().getStartTime();
        LocalDateTime endTime = entity.getTimeSlot().getStartTime()
                .plusMinutes(entity.getTimeSlot().getDurationMinutes());

        if (consultationRepository.existsByOverlapping(startTime,endTime,doctor.getId(),ConsultationStatus.CANCELLED)){
            throw new ConsultationException("Consultation time slot is already booked");
        }
        entity.getTimeSlot().setEndTime(endTime);


        Consultation consultation = consultationRepository.save(entity);

        log.info("Consultation added {} by {}", consultation.getId(), doctor.getEmail());

        eventPublisher.publishEvent(new ConsultationAddedEvent(
                consultation.getId()
                ,consultation.getDoctor().getId()
                ,consultation.getTimeSlot().getStartTime()));
        return consultation;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Consultation> getDoctorSchedule(ScheduleFilter filter, Pageable pageable) {
        Specification<Consultation> spec = ScheduleSpecification.filter(filter, authService.getAuthenticateUserId());
        return consultationRepository.findAll(spec,pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public Consultation editConsultation(AddConsultationRequest request, UUID id) {
        UUID userId = authService.getAuthenticateUserId();

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(()->new ConsultationException("Consultation not found"));

        validateEditability(consultation, userId);

        doctorRepository.findByIdWithLock(userId);

        LocalDateTime start = request.startTime();
        LocalDateTime end = request.startTime().plusMinutes(request.duration());
        if (consultationRepository.existsByOverlapping(start,end,userId,id,ConsultationStatus.CANCELLED)){
            throw new ConsultationException("Consultation time slot is already booked");
        }
        consultation.getTimeSlot().setStartTime(start);
        consultation.getTimeSlot().setEndTime(end);
        log.info("Consultation edited {} by {}", consultation.getId(),userId);


        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit(){
                        messagingTemplate.convertAndSend("/topic/consultation"
                                ,new ConsultationChanged(consultation.getId(),start));
                    }
                }
        );

        return  consultationRepository.save(consultation);
    }

    private static void validateEditability(Consultation consultation, UUID userId) {
        if (consultation.getStatus().isTerminal()){
            throw new ConsultationException("Consultation is already completed");
        }
        if (consultation.getStatus()!=ConsultationStatus.AVAILABLE){
            throw new ConsultationException("You cannot edit on booked consultation");
        }
        if (!consultation.getDoctor().getId().equals(userId)){
            throw new ConsultationException("You are not authorized to edit this consultation");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void cancel(UUID id, String reason) {
        Consultation consultation = consultationRepository.findConsultationById(id)
                .orElseThrow(()->new ConsultationException("Consultation not found"));

        User currentUser = authService.getAuthenticateUser();
        if (consultation.getStatus().isTerminal()) {
            throw new ConsultationException("Consultation is already completed");
        }

        if (cancelAvailableConsultation(consultation, currentUser)) return;

        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && !consultation.getPatient().getId().equals(currentUser.getId())) {
            throw new ConsultationException("You are not authorized to cancel this consultation");
        }


        boolean cancelledByPatient = cancelBookedConsultation(reason, consultation, currentUser);

        log.info("Consultation cancelled {} by {}", consultation.getId(), currentUser.getEmail());

        // TODO handle refund Logic
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit(){
                        if (cancelledByPatient) {
                            messagingTemplate.convertAndSend("/topic/consultation",new ConsultationCancelled(id,ConsultationStatus.AVAILABLE));
                        }
                    }
                }
        );


        Doctor doctor = consultation.getDoctor();
        var patient = consultation.getPatient();
        eventPublisher.publishEvent(new ConsultationCanceled(id, doctor.getEmail(),
                doctor.getFirstName(),
                patient != null ? patient.getEmail() : "",
                patient != null ? patient.getFirstName() : "", cancelledByPatient, reason));

    }

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
                        messagingTemplate.convertAndSend("/topic/consultation",
                                new ConsultationEvent(consultation.getId(), EventType.BOOKED, Map.of()));
                    }
                }
        );

        return clientSecret;
    }

    private void checkPatientOverlapping(Consultation consultation, Patient currentUser) {
        if (consultationRepository.existsByPatientOverlapping(consultation.getTimeSlot().getStartTime(),
                consultation.getTimeSlot().getEndTime(),currentUser.getId(),ConsultationStatus.AVAILABLE)){
            throw new ConsultationException("Consultation time slot is already booked");
        }
    }

    private boolean cancelBookedConsultation(String reason, Consultation consultation, User currentUser) {
        CancellationLog cancellationLog = CancellationLog.builder()
                .consultation(consultation)
                .cancelledBy(currentUser)
                .reason(reason).build();

        cancellationLogRepository.save(cancellationLog);

        boolean cancelledByPatient = currentUser.getId().equals(consultation.getPatient().getId());
        consultation.setStatus(cancelledByPatient ? ConsultationStatus.AVAILABLE : ConsultationStatus.CANCELLED);

        consultation.setCancellationLog(cancellationLog);
        consultationRepository.save(consultation);
        return cancelledByPatient;
    }

    private boolean cancelAvailableConsultation(Consultation consultation, User currentUser) {
        if (consultation.getStatus() == ConsultationStatus.AVAILABLE) {
            if (!consultation.getDoctor().getId().equals(currentUser.getId())) {
                throw new ConsultationException("You are not authorized to cancel this consultation");
            }
            consultation.setStatus(ConsultationStatus.CANCELLED);
            consultationRepository.save(consultation);
            messagingTemplate.convertAndSend("/topic/consultation",
                    new ConsultationCancelled(consultation.getId(),ConsultationStatus.CANCELLED));
            return true;
        }
        return false;
    }
}
