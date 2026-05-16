package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

public interface IConsultationSlotHoldingService {
    boolean hold(UUID slotId) throws InterruptedException;
    void release(UUID slotId);
}
