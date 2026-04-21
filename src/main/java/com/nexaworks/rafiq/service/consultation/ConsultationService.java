package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.entities.Consultation;

public interface ConsultationService {
    Consultation add(AddConsultationRequest request);
}
