package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public interface IConsultationSearchService {

    Consultation getConsultation(UUID id);

    Page<ConsultationSlot> getDoctorSchedule(ScheduleFilter filter, Pageable pageable);

    Page<Consultation> getPatientConsultationsByStatus(ConsultationStatus status,
            Pageable pageable);

    Page<ConsultationSlot> getDoctorUpcoming(Pageable pageable);

    Page<DoctorConsultationResponse> getDoctorAvailableSlots(UUID id, Pageable pageable);

    ConsultationSlot getConsultationSlot(UUID id);
}
