package com.nexaworks.rafiq.service.summary;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.nexaworks.rafiq.dto.request.summary.ConsultationSummaryFilter;
import com.nexaworks.rafiq.dto.request.summary.CreateConsultationSummaryRequest;
import com.nexaworks.rafiq.dto.request.summary.UpdateConsultationSummaryRequest;
import com.nexaworks.rafiq.dto.response.summary.ConsultationSummaryResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSummary;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.exception.custom.auth.AuthorizationException;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationInvalidException;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationSummaryIsAlreadyCreated;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationSummaryNotFoundException;
import com.nexaworks.rafiq.exception.custom.general.InvalidRequestException;
import com.nexaworks.rafiq.mapper.ConsultationSummaryMapper;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.ConsultationSummaryRepository;
import com.nexaworks.rafiq.repository.specification.ConsultationSummarySpecification;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationSummaryServiceImpl implements ConsultationSummaryService {

    private static final List<ConsultationStatus> LIST_GATE_EXCLUDED_STATUSES = List
            .of(ConsultationStatus.COMPLETED, ConsultationStatus.CANCELLED);

    private final ConsultationSummaryRepository consultationSummaryRepository;
    private final ConsultationRepository consultationRepository;
    private final ConsultationSummaryMapper consultationSummaryMapper;
    private final AuthService authService;

    private static boolean hasAuthority(User user, String authority) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    @Override
    @Transactional
    public ConsultationSummaryResponse create(CreateConsultationSummaryRequest request) {
        User user = authService.getAuthenticateUser();
        UUID doctorId = user.getId();
        Consultation consultation = consultationRepository.findById(request.consultationId())
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));

        validateConsultation(consultation, doctorId);

        ConsultationSummary entity = consultationSummaryMapper.toEntity(request);
        entity.setPatient(consultation.getPatient());
        entity.setConsultation(consultation);
        return consultationSummaryMapper.toResponse(consultationSummaryRepository.save(entity));
    }

    private void validateConsultation(Consultation consultation, UUID doctorId) {
        if (consultation.getStatus() != ConsultationStatus.COMPLETED) {
            throw new ConsultationInvalidException(
                    "Consultation must be completed before adding a summary");
        }
        if (!consultation.getDoctor().getId().equals(doctorId)) {
            throw new AuthorizationException("You are not the doctor for this consultation");
        }
        if (consultation.getPatient() == null) {
            throw new ConsultationInvalidException("Consultation has no patient");
        }
        if (consultationSummaryRepository.findByConsultationId(consultation.getId()).isPresent()) {
            throw new ConsultationSummaryIsAlreadyCreated(
                    "Summary already exists for this consultation");
        }
    }

    @Override
    public ConsultationSummaryResponse get(UUID id) {
        ConsultationSummary summary = consultationSummaryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Consultation summary not found"));
        assertParticipantOrThrow(summary);
        return consultationSummaryMapper.toResponse(summary);
    }

    @Override
    @Transactional
    public ConsultationSummaryResponse update(UUID id, UpdateConsultationSummaryRequest request) {
        User user = authService.getAuthenticateUser();
        ConsultationSummary summary = consultationSummaryRepository.findById(id).orElseThrow(
                () -> new ConsultationSummaryNotFoundException("Consultation summary not found"));
        if (!summary.getDoctor().getId().equals(user.getId())) {
            throw new AuthorizationException("Only the authoring doctor can update this summary");
        }
        consultationSummaryMapper.updateEntity(request, summary);
        return consultationSummaryMapper.toResponse(consultationSummaryRepository.save(summary));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = authService.getAuthenticateUser();
        UUID userId = user.getId();
        ConsultationSummary summary = consultationSummaryRepository.findById(id).orElseThrow(
                () -> new ConsultationSummaryNotFoundException("Consultation summary not found"));
        boolean doctor = summary.getDoctor().getId().equals(userId);
        boolean patient = summary.getPatient().getId().equals(userId);
        if (!doctor && !patient) {
            throw new AuthorizationException("Only the authoring doctor can update this summary");
        }
        summary.delete(String.valueOf(userId));
        consultationSummaryRepository.save(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationSummaryResponse> list(UUID patientIdParam,
            Specialization specialization, Pageable pageable) {
        User user = authService.getAuthenticateUser();
        boolean patientRole = hasAuthority(user, "ROLE_PATIENT");
        boolean doctorRole = hasAuthority(user, "ROLE_DOCTOR");

        if (patientIdParam != null && patientRole) {
            throw new InvalidRequestException("patientId must not be sent by patients");
        }
        if (doctorRole && patientIdParam == null) {
            throw new AuthorizationException(
                    "Doctors must provide patientId to list consultation summaries");
        }
        ConsultationSummaryFilter listFilter = new ConsultationSummaryFilter(specialization);
        if (patientRole) {
            Specification<ConsultationSummary> spec = ConsultationSummarySpecification
                    .filter(listFilter, user.getId());
            return consultationSummaryRepository.findAll(spec, pageable)
                    .map(consultationSummaryMapper::toResponse);
        }
        if (doctorRole) {
            boolean gateOk = consultationRepository.existsByDoctorAndPatientAndStatusNotIn(
                    user.getId(), patientIdParam, LIST_GATE_EXCLUDED_STATUSES);
            if (!gateOk) {
                throw new AuthorizationException("No qualifying consultation with this patient");
            }
            Specification<ConsultationSummary> spec = ConsultationSummarySpecification
                    .filter(listFilter, patientIdParam);
            return consultationSummaryRepository.findAll(spec, pageable)
                    .map(consultationSummaryMapper::toResponse);
        }
        throw new AuthorizationException("Unsupported role");
    }

    private void assertParticipantOrThrow(ConsultationSummary summary) {
        UUID userId = authService.getAuthenticateUserId();
        if (!summary.getPatient().getId().equals(userId)
                && !summary.getDoctor().getId().equals(userId)) {
            throw new AuthorizationException("You cannot access this consultation summary");
        }
    }
}
