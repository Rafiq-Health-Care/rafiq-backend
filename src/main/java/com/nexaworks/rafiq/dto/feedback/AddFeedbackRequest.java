package com.nexaworks.rafiq.dto.feedback;

import java.util.UUID;

public record AddFeedbackRequest(double rating, String comment, UUID consultationId) {
}
