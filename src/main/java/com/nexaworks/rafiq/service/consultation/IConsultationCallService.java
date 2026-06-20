package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import com.nexaworks.rafiq.dto.response.consultation.CallResponse;

public interface IConsultationCallService {
    CallResponse enterCall(UUID consultationId);
}
