package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.*;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public interface IConsultationSearchService {

    ConsultationResponse getConsultation(UUID id);

    PageResponse<ScheduleResponse> getDoctorSchedule(ScheduleFilter filter, Pageable pageable);

    PageResponse<PatientConsultationResponse> getPatientConsultationsByStatus(
            ConsultationStatus status, Pageable pageable);

    PageResponse<ConsultationSlotResponse> getDoctorUpcoming(Pageable pageable);

    PageResponse<DoctorConsultationResponse> getDoctorAvailableSlots(UUID id, Pageable pageable);

    ConsultationSlotResponse getConsultationSlot(UUID id);
}
