package com.nexaworks.rafiq.service.consultation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.consultation.CallResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationFilter;
import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.exception.custom.ConsultationNotFoundException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.specification.ConsultationSpecification;
import com.nexaworks.rafiq.repository.specification.ScheduleSpecification;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConsultationSearchService implements IConsultationSearchService {
    private final ConsultationRepository consultationRepository;
    private final AuthService authService;

    @Override
    public Page<Consultation> getConsultations(ConsultationFilter filter, Pageable pageable) {
        Specification<Consultation> spec = ConsultationSpecification.filterConsultation(filter);

        return consultationRepository.findAll(spec, pageable);
    }

    @Override
    public CallResponse getConsultationCall(UUID id) {
        return consultationRepository.getConsultationCallInfo(id);
    }

    @Override
    public Consultation getConsultation(UUID id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));
    }
    @Override
    @Transactional(readOnly = true)
    public Page<Consultation> getDoctorSchedule(ScheduleFilter filter, Pageable pageable) {
        Specification<Consultation> spec = ScheduleSpecification.filter(filter,
                authService.getAuthenticateUserId());
        return consultationRepository.findAll(spec, pageable);
    }

    @Override
    public List<Consultation> getPatientUpcoming() {
        UUID patientId = authService.getAuthenticateUserId();
        return consultationRepository.findAllByPatientIdAndStatus(patientId,
                ConsultationStatus.CONFIRMED, ConsultationStatus.ONGOING);
    }

    @Override
    public List<Consultation> getDoctorUpcoming() {
        UUID doctorId = authService.getAuthenticateUserId();
        return consultationRepository.findAllDoctorUpcoming(doctorId, ConsultationStatus.CONFIRMED,
                ConsultationStatus.ONGOING);
    }

    @Override
    public List<Consultation> getPatientConsultation(ConsultationStatus status) {
        return consultationRepository
                .findAllPatientConsultation(authService.getAuthenticateUserId(), status);

    }

    @Override
    public List<DoctorConsultationResponse> getDoctorAvailableConsultation(UUID id) {
        return consultationRepository.getDoctorAvailableConsultation(id,
                ConsultationStatus.AVAILABLE);
    }

}
