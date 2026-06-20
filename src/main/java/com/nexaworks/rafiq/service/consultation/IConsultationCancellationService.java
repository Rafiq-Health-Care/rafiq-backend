package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

public interface IConsultationCancellationService {
    void cancel(UUID id, String reason);
}
