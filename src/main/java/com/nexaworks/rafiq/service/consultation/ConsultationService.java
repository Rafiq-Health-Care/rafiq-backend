package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.entities.Consultation;

import java.util.List;
import java.util.UUID;

public interface ConsultationService {
    Consultation add(Consultation request);

    List<Consultation> getDoctorSchedule(ScheduleFilter filter);

    Consultation editConsultation(AddConsultationRequest request, UUID id);

    void cancel(UUID id, String reason);
}
