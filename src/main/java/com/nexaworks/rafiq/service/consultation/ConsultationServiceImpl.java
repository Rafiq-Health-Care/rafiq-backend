package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.event.ConsultationAddedEvent;
import com.nexaworks.rafiq.dto.event.ConsultationCanceled;
import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.entities.CancellationLog;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.exception.custom.ConsultationException;
import com.nexaworks.rafiq.repository.CancellationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.specification.ScheduleSpecification;
import com.nexaworks.rafiq.service.authentication.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService{
    private final ConsultationRepository consultationRepository;
    private final CancellationLogRepository cancellationLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthService authService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Consultation add(Consultation entity) {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();
        entity.setDoctor(doctor);
        entity.setDoctor(doctor);

        LocalTime startTime = entity.getTimeSlot().getStartTime();
        LocalTime endTime = entity.getTimeSlot().getStartTime()
                .plusMinutes(entity.getTimeSlot().getDurationMinutes());

        if (consultationRepository.existsByOverlapping(startTime,endTime,doctor.getId())){
            throw new ConsultationException("Consultation time slot is already booked");
        }
        entity.getTimeSlot().setEndTime(endTime);


        Consultation consultation = consultationRepository.save(entity);

        log.info("Consultation added {}", consultation.getId());

        eventPublisher.publishEvent(new ConsultationAddedEvent(
                consultation.getId()
                ,consultation.getDoctor().getId()
                ,consultation.getTimeSlot().getDate()));
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
    public Consultation editConsultation(AddConsultationRequest request, UUID id) {
        UUID userId = authService.getAuthenticateUserId();

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(()->new ConsultationException("Consultation not found"));

        if (consultation.getStatus().isTerminal()){
            throw new ConsultationException("Consultation is already completed");
        }
        if (consultation.getStatus()!=ConsultationStatus.AVAILABLE){
            throw new ConsultationException("You cannot edit on booked consultation");
        }
        if (!consultation.getDoctor().getId().equals(userId)){
            throw new ConsultationException("You are not authorized to edit this consultation");
        }
        LocalTime start = request.startTime();
        LocalTime end = request.startTime().plusMinutes(request.duration());
        if (consultationRepository.existsByOverlapping(start,end,userId,id)){
            throw new ConsultationException("Consultation time slot is already booked");
        }
        consultation.getTimeSlot().setStartTime(start);
        consultation.getTimeSlot().setEndTime(end);
        log.info("Consultation edited {}", consultation.getId());
        // TODO handle real time connections
        return  consultationRepository.save(consultation);
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
        if (!consultation.getDoctor().getId().equals(currentUser.getId())&&!consultation.getPatient().getId().equals(currentUser.getId())){
            throw new ConsultationException("You are not authorized to cancel this consultation");
        }

        if (consultation.getStatus().isTerminal()){
            throw new ConsultationException("Consultation is already completed");
        }


        CancellationLog cancellationLog = CancellationLog.builder()
                .consultation(consultation)
                .cancelledBy(currentUser)
                .reason(reason).build();

        cancellationLogRepository.save(cancellationLog);

        boolean cancelledByPatient = currentUser.getId().equals(consultation.getPatient().getId());
        consultation.setStatus(cancelledByPatient ? ConsultationStatus.AVAILABLE : ConsultationStatus.CANCELLED);

        consultation.setCancellationLog(cancellationLog);
        consultationRepository.save(consultation);

        // TODO handle refund Logic
        // TODO handle real time connections

        eventPublisher.publishEvent(new ConsultationCanceled(
                id, consultation.getDoctor().getId(),
                consultation.getPatient().getId(),
                currentUser.getId(), reason
        ));

    }
}
