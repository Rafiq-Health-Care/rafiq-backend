package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.event.ConsultationAddedEvent;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService{
    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthService authService;


    @Override
    @Transactional(rollbackOn = Exception.class)
    public Consultation add(Consultation entity) {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();
        entity.setDoctor(doctor);
        // TODO validate overlapping time slots

        Consultation consultation = consultationRepository.save(entity);
        eventPublisher.publishEvent(new ConsultationAddedEvent(
                consultation.getId()
                ,consultation.getDoctor().getId()
                ,consultation.getTimeSlot().getDate()));
        return consultation;
    }
}
