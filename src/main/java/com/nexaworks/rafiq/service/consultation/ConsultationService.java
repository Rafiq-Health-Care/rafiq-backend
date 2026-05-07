package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ConsultationService {
    Consultation add(Consultation request);

    Page<Consultation> getDoctorSchedule(ScheduleFilter filter, Pageable pageable);

    Consultation editConsultation(AddConsultationRequest request, UUID id);

    void cancel(UUID id, String reason);

    void expire(String consultationId);

}
