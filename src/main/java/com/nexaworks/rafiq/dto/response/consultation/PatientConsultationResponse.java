package com.nexaworks.rafiq.dto.response.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Consultation details from the patient's perspective")
public record PatientConsultationResponse(

        @Schema(description = "Unique identifier of the consultation", example = "123e4567-e89b-12d3-a456-426614174000") UUID consultationId,
        @Schema(description = "Full name of the doctor", example = "Dr. John Smith") String doctorName,

        @Schema(description = "Doctor's biography", example = "10 years of experience in cardiology") String doctorBio,

        @Schema(description = "URL of the doctor's profile image", example = "https://example.com/images/doctor.jpg") String doctorImage,

        @Schema(description = "Start time of the consultation", example = "2025-06-01T09:00:00") LocalDateTime startTime,

        @Schema(description = "Duration in minutes", example = "30") int duration,

        @Schema(description = "UUID of the consultation summary, null if not yet available", example = "789e1234-e89b-12d3-a456-426614174999") UUID summaryId,

        @Schema(description = "Unique identifier of the doctor", example = "456e7890-e89b-12d3-a456-426614174111") UUID doctorId) {
}
