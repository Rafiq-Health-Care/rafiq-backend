package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.*;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
import com.nexaworks.rafiq.mapper.ConsultationMapper;
import com.nexaworks.rafiq.mapper.ConsultationSlotMapper;
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
    private final ConsultationMapper consultationMapper;
    private final ConsultationSlotMapper consultationSlotMapper;

    @Override
    @Cacheable(key = "#id", value = "consultation", unless = "#result.status().name()!='UPCOMING'")
    public ConsultationResponse getConsultation(UUID id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));
        return consultationMapper.toDto(consultation);
    }
    @Override
    public PageResponse<ScheduleResponse> getDoctorSchedule(ScheduleFilter filter,
            Pageable pageable) {
        Specification<ConsultationSlot> spec = ScheduleSpecification.filter(filter,
                authService.getAuthenticateUserId());
        Page<ConsultationSlot> slotPage = consultationSlotRepository.findAll(spec, pageable);
        return PageResponse.of(slotPage, consultationSlotMapper::toScheduleDto);
    }

    @Override
    public PageResponse<PatientConsultationResponse> getPatientConsultationsByStatus(
            ConsultationStatus status, Pageable pageable) {
        UUID patientId = authService.getAuthenticateUserId();
        Page<Consultation> consultationPage = consultationRepository
                .findAllByPatientIdAndStatus(patientId, status, pageable);
        return PageResponse.of(consultationPage, consultationMapper::toPatientResponse);
    }

    @Override
    public PageResponse<ConsultationSlotResponse> getDoctorUpcoming(Pageable pageable) {
        UUID doctorId = authService.getAuthenticateUserId();
        Page<ConsultationSlot> slotPage = consultationSlotRepository.findAllDoctorUpcoming(doctorId,
                SlotStatus.BOOKED, pageable);
        return PageResponse.of(slotPage, consultationSlotMapper::toDto);
    }

    @Override
    public PageResponse<DoctorConsultationResponse> getDoctorAvailableSlots(UUID id,
            Pageable pageable) {
        Page<DoctorConsultationResponse> slotPage = consultationSlotRepository
                .getDoctorAvailableConsultation(id, SlotStatus.AVAILABLE, pageable);
        return PageResponse.of(slotPage, response -> response);
    }

    @Override
    public ConsultationSlotResponse getConsultationSlot(UUID id) {
        ConsultationSlot slot = consultationSlotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));
        return consultationSlotMapper.toDto(slot);
    }

    @Override
    public Consultation getConsultationEntity(UUID consultationId) {
        return consultationRepository
                .findConsultationByIdAndPatientId(consultationId,
                        authService.getAuthenticateUserId())
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));
    }

}
