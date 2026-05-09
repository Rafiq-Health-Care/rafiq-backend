package com.nexaworks.rafiq.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.*;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.mapper.ConsultationMapper;
import com.nexaworks.rafiq.service.consultation.ConsultationSearchService;
import com.nexaworks.rafiq.service.consultation.ConsultationService;
import com.nexaworks.rafiq.service.consultation.ReservationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/consultation")
@RequiredArgsConstructor
@Tag(name = "Consultation", description = "Endpoints for consultation")
public class ConsultationController {
    private final ConsultationService consultationService;
    private final ConsultationSearchService consultationSearchService;
    private final ReservationService reservationService;
    private final ConsultationMapper mapper;

    @PostMapping("/add")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ConsultationResponse> addConsultation(
            @Valid @RequestBody AddConsultationRequest request) {
        Consultation consultation = mapper.toEntity(request);
        consultation = consultationService.add(consultation);
        return ResponseEntity.ok(mapper.toDto(consultation));

    }
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PageResponse<ScheduleResponse>> getSchedule(
            @Valid @RequestBody ScheduleFilter filter, Pageable pageable) {
        Page<Consultation> consultations = consultationSearchService.getDoctorSchedule(filter,
                pageable);
        return ResponseEntity.ok(mapper.toSchedulePageResponse(consultations));
    }
    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ConsultationResponse> editConsultation(
            @Valid @RequestBody AddConsultationRequest request, @PathVariable UUID id) {
        Consultation consultation = consultationService.editConsultation(request, id);
        return ResponseEntity.ok(mapper.toDto(consultation));
    }
    @PatchMapping("/cancel/{id}")
    public ResponseEntity<?> cancelConsultation(@PathVariable UUID id,
            @RequestParam String reason) {
        consultationService.cancel(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reserve/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> reserveConsultation(@PathVariable UUID id,
            @RequestParam PaymentProvider provider) {
        String paymentKey = reservationService.reserve(id, provider);
        return ResponseEntity.ok(paymentKey);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> getConsultation(@PathVariable UUID id) {
        Consultation consultation = consultationSearchService.getConsultation(id);
        return ResponseEntity.ok(mapper.toDto(consultation));
    }

    @GetMapping("/{id}/call")
    public ResponseEntity<CallResponse> getCall(@PathVariable UUID id) {
        return ResponseEntity.ok(consultationSearchService.getConsultationCall(id));
    }

    @PostMapping("/filter")
    public ResponseEntity<PageResponse<ConsultationResponse>> filterConsultation(
            @RequestBody ConsultationFilter filter, Pageable pageable) {

        Page<Consultation> pageableConsultation = consultationSearchService.getConsultations(filter,
                pageable);

        return ResponseEntity.ok(mapper.toPageResponse(pageableConsultation));
    }

    @GetMapping("/patient/upcoming")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PatientConsultationResponse>> getPatientUpcoming() {
        List<Consultation> upcomingConsultation = consultationSearchService.getPatientUpcoming();

        return ResponseEntity.ok(mapper.toPatientDtoList(upcomingConsultation));
    }

    @GetMapping("/doctor/upcoming")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<ConsultationResponse>> getDoctorUpcoming() {
        List<Consultation> upcomingConsultation = consultationSearchService.getDoctorUpcoming();

        return ResponseEntity.ok(mapper.toDtoList(upcomingConsultation));
    }
    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<DoctorConsultationResponse>> getDoctorConsultations(
            @PathVariable UUID id) {

        return ResponseEntity.ok(consultationSearchService.getDoctorAvailableConsultation(id));
    }

    @PostMapping("/patient/my-consultations/{status}")
    public ResponseEntity<List<PatientConsultationResponse>> getPatientConsultations(
            @PathVariable ConsultationStatus status) {
        List<Consultation> consultations = consultationSearchService.getPatientConsultation(status);
        return ResponseEntity.ok(mapper.toPatientDtoList(consultations));
    }

}
