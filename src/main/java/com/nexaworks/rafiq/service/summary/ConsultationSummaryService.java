package com.nexaworks.rafiq.service.summary;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.summary.CreateConsultationSummaryRequest;
import com.nexaworks.rafiq.dto.request.summary.UpdateConsultationSummaryRequest;
import com.nexaworks.rafiq.dto.response.summary.ConsultationSummaryResponse;
import com.nexaworks.rafiq.entities.enums.Specialization;

public interface ConsultationSummaryService {

    ConsultationSummaryResponse create(CreateConsultationSummaryRequest request);

    ConsultationSummaryResponse get(UUID id);

    ConsultationSummaryResponse update(UUID id, UpdateConsultationSummaryRequest request);

    void delete(UUID id);

    Page<ConsultationSummaryResponse> list(UUID patientId, Specialization specialization,
            Pageable pageable);
}
