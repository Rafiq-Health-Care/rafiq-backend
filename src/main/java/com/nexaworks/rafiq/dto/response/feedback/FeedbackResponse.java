package com.nexaworks.rafiq.dto.response.feedback;

import java.time.LocalDate;

public record FeedbackResponse(double rating, String comment, String patientName,
        LocalDate createdAt) {
}
