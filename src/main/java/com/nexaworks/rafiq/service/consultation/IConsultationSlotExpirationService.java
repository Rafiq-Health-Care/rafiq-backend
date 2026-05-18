package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

public interface IConsultationSlotExpirationService {
    void expire(UUID consultationSlotId);
}
