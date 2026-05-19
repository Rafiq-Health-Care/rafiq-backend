package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.request.consultation.ReserveConsultationRequest;

public interface IReservationService {
    String reserve(ReserveConsultationRequest request);
}
