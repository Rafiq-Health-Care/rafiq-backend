package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.entities.ConsultationSlot;

public interface IConsultationSlotService {
    ConsultationSlot add(AddConsultationRequest request);

    ConsultationSlot editConsultation(AddConsultationRequest request, UUID id);

}
