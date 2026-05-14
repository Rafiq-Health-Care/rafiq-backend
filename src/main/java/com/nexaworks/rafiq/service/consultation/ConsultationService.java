package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public interface ConsultationService {
    Consultation add(Consultation request);

    Consultation editConsultation(AddConsultationRequest request, UUID id);

    void expire(String consultationId);

    void update(UUID id, ConsultationStatus consultationStatus);
}
