package com.nexaworks.rafiq.service.consultation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public interface IConsultationSearchService {

    ConsultationSlot getConsultation(UUID id);

    Page<ConsultationSlot> getDoctorSchedule(ScheduleFilter filter, Pageable pageable);

    List<Consultation> getPatientUpcoming();

    List<ConsultationSlot> getDoctorUpcoming();

    List<Consultation> getPatientConsultation(ConsultationStatus status);

    List<DoctorConsultationResponse> getDoctorAvailableConsultation(UUID id);
}
