package com.nexaworks.rafiq.service.feedback;

import java.util.UUID;

import com.nexaworks.rafiq.dto.feedback.AddFeedbackRequest;

import jakarta.validation.Valid;

public interface IFeedbackService {
    void addFeedback(@Valid AddFeedbackRequest request);

    void deleteFeedback(UUID feedbackId);
}
