package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

public interface IConsultationSlotHoldingService {
    void hold(UUID slotId);
    void release(UUID slotId);
}
