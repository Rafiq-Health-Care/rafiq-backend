package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService{
    private final ConsultationRepository consultationRepository;

    @Override
    public Consultation add(AddConsultationRequest request) {
        return null;
    }
}
