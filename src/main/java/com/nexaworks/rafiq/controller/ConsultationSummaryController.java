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

import dev.once.annotation.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/consultation-summary")
@RequiredArgsConstructor
@Tag(name = "Consultation summary", description = "Post-consultation summaries")
public class ConsultationSummaryController {

    private final ConsultationSummaryService consultationSummaryService;

    @Idempotent(force = true)
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create consultation summary", description = "Creates a medical consultation summary. Accessible only by doctors.", responses = {
            @ApiResponse(responseCode = "200", description = "Consultation summary created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsultationSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Consultation summary already exists"),
            @ApiResponse(responseCode = "404", description = "Consultation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<ConsultationSummaryResponse> create(
            @Valid @RequestBody CreateConsultationSummaryRequest request) {
        return ResponseEntity.ok(consultationSummaryService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    @Operation(summary = "Get consultation summary by id", description = "Retrieves a consultation summary. Accessible by patient and doctor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation summary retrieved successfully", content = @Content(schema = @Schema(implementation = ConsultationSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Consultation summary not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<ConsultationSummaryResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(consultationSummaryService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update consultation summary", description = "Updates an existing consultation summary. Accessible only by doctors.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation summary updated successfully", content = @Content(schema = @Schema(implementation = ConsultationSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Consultation summary not found")})
    public ResponseEntity<ConsultationSummaryResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateConsultationSummaryRequest request) {
        return ResponseEntity.ok(consultationSummaryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    @Operation(summary = "Delete consultation summary", description = "Deletes a consultation summary. Accessible by patient and doctor.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Consultation summary deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Consultation summary not found")})
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        consultationSummaryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    @Operation(summary = "List consultation summaries", description = "Retrieves paginated consultation summaries with optional filters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation summaries retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<PageResponse<ConsultationSummaryResponse>> list(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) Specialization specialization, Pageable pageable) {
        Page<ConsultationSummaryResponse> page = consultationSummaryService.list(patientId,
                specialization, pageable);

        return ResponseEntity.ok(new PageResponse<>(page.getContent(), page.getNumberOfElements(),
                page.getSize(), page.getTotalPages(), page.isLast(), page.isFirst()));
    }
}
