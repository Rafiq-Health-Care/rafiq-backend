package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.entities.Consultation;

public interface ConsultationService {
    Consultation add(Consultation request);

    Consultation editConsultation(AddConsultationRequest request, UUID id);

    void cancel(UUID id, String reason);

    void expire(String consultationId);

}
