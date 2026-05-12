package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.summary.CreateConsultationSummaryRequest;
import com.nexaworks.rafiq.dto.request.summary.UpdateConsultationSummaryRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.summary.ConsultationSummaryResponse;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.service.summary.ConsultationSummaryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/consultation-summary")
@RequiredArgsConstructor
@Tag(name = "Consultation summary", description = "Post-consultation summaries")
public class ConsultationSummaryController {

    private final ConsultationSummaryService consultationSummaryService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ConsultationSummaryResponse> create(
            @Valid @RequestBody CreateConsultationSummaryRequest request) {
        return ResponseEntity.ok(consultationSummaryService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<ConsultationSummaryResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(consultationSummaryService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ConsultationSummaryResponse> update(@PathVariable UUID id,
            @RequestBody UpdateConsultationSummaryRequest request) {
        return ResponseEntity.ok(consultationSummaryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        consultationSummaryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<PageResponse<ConsultationSummaryResponse>> list(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) Specialization specialization, Pageable pageable) {
        Page<ConsultationSummaryResponse> page = consultationSummaryService.list(patientId,
                specialization, pageable);
        return ResponseEntity.ok(new PageResponse<>(page.getContent(), page.getNumberOfElements(),
                page.getSize(), page.getTotalPages(), page.isLast(), page.isFirst()));
    }
}
