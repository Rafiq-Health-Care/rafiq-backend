package com.nexaworks.rafiq.service.consultation;

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
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
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
    public Consultation getConsultation(UUID id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));
    }
    @Override
    public Page<ConsultationSlot> getDoctorSchedule(ScheduleFilter filter, Pageable pageable) {
        Specification<ConsultationSlot> spec = ScheduleSpecification.filter(filter,
                authService.getAuthenticateUserId());
        return consultationSlotRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Consultation> getPatientConsultationsByStatus(ConsultationStatus status,
            Pageable pageable) {
        UUID patientId = authService.getAuthenticateUserId();
        return consultationRepository.findAllByPatientIdAndStatus(patientId, status, pageable);
    }

    @Override
    public Page<ConsultationSlot> getDoctorUpcoming(Pageable pageable) {
        UUID doctorId = authService.getAuthenticateUserId();
        return consultationSlotRepository.findAllDoctorUpcoming(doctorId, SlotStatus.BOOKED,
                pageable);
    }

    @Override
    public Page<DoctorConsultationResponse> getDoctorAvailableSlots(UUID id, Pageable pageable) {
        return consultationSlotRepository.getDoctorAvailableConsultation(id, SlotStatus.AVAILABLE,
                pageable);
    }

    @Override
    public ConsultationSlot getConsultationSlot(UUID id) {
        return consultationSlotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));
    }

}
