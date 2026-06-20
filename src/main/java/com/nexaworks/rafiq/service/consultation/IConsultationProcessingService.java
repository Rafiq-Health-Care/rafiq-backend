package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

public interface IConsultationProcessingService {
    void success(UUID id);
    void failed(UUID id);
}
