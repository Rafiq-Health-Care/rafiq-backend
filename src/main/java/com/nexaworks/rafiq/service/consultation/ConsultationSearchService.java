package com.nexaworks.rafiq.service.consultation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.SlotNotFoundException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;
import com.nexaworks.rafiq.repository.specification.ScheduleSpecification;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConsultationSearchService implements IConsultationSearchService {
    private final ConsultationSlotRepository consultationSlotRepository;
    private final ConsultationRepository consultationRepository;
    private final AuthService authService;

    @Override
    public ConsultationSlot getConsultation(UUID id) {
        return consultationSlotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));
    }
    @Override
    public Page<ConsultationSlot> getDoctorSchedule(ScheduleFilter filter, Pageable pageable) {
        Specification<ConsultationSlot> spec = ScheduleSpecification.filter(filter,
                authService.getAuthenticateUserId());
        return consultationSlotRepository.findAll(spec, pageable);
    }

    @Override
    public List<Consultation> getPatientUpcoming() {
        UUID patientId = authService.getAuthenticateUserId();
        return consultationRepository.findAllByPatientIdAndStatus(patientId,
                ConsultationStatus.CONFIRMED, ConsultationStatus.ONGOING);
    }

    @Override
    public List<ConsultationSlot> getDoctorUpcoming() {
        UUID doctorId = authService.getAuthenticateUserId();
        return consultationSlotRepository.findAllDoctorUpcoming(doctorId, SlotStatus.BOOKED);
    }

    @Override
    public List<Consultation> getPatientConsultation(ConsultationStatus status) {
        return consultationRepository
                .findAllPatientConsultation(authService.getAuthenticateUserId(), status);

    }

    @Override
    public List<DoctorConsultationResponse> getDoctorAvailableConsultation(UUID id) {
        return consultationSlotRepository.getDoctorAvailableConsultation(id,
                ConsultationStatus.AVAILABLE);
    }

}
