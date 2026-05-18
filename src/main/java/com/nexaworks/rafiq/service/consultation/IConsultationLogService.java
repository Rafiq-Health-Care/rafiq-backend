package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.entities.Consultation;

public interface IConsultationLogService {
    void logEnter(Consultation consultation);

    void logLeave(Consultation consultation);
}
