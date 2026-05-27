package com.nexaworks.rafiq.service.feedback;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.dto.feedback.AddFeedbackRequest;
import com.nexaworks.rafiq.dto.response.feedback.FeedbackResponse;

import jakarta.validation.Valid;

public interface IFeedbackService {
    void addFeedback(@Valid AddFeedbackRequest request);

    void deleteFeedback(UUID feedbackId);

    List<FeedbackResponse> getFeedbackByDoctorId(UUID doctorId);

    FeedbackResponse getFeedbackByConsultationId(UUID consultationId);
}
