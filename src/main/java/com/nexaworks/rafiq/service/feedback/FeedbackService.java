package com.nexaworks.rafiq.service.feedback;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.feedback.AddFeedbackRequest;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Feedback;
import com.nexaworks.rafiq.repository.FeedbackRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.consultation.IConsultationSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final IConsultationSearchService consultationSearchService;
    private final AuthService authService;

    @Override
    @Transactional
    public void addFeedback(AddFeedbackRequest request) {
        Consultation consultation = consultationSearchService
                .getConsultationEntity(request.consultationId());
        Feedback feedback = Feedback.builder().patient(consultation.getPatient())
                .doctor(consultation.getDoctor()).consultation(consultation)
                .rating(request.rating()).feedback(request.comment()).build();
        feedbackRepository.save(feedback);
    }

    @Override
    public void deleteFeedback(UUID feedbackId) {
        feedbackRepository.deleteByIdAndPatientId(feedbackId, authService.getAuthenticateUserId());
    }
}
