package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.response.consultation.CallResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationNotFoundException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.payout.PayoutService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultationCallService implements IConsultationCallService {
    private final ConsultationRepository consultationRepository;
    private final ConsultationPreparationService preparationService;
    private final ConsultationLogService consultationLogService;
    private final AuthService authService;
    private final PayoutService payoutService;
    @Override
    public CallResponse enterCall(UUID consultationId) {
        log.info("Getting call for consultation: {}", consultationId);
        Consultation consultation = getConsultation(consultationId);

        String accessToken = consultation.getAccessToken();
        if (accessToken == null) {
            accessToken = preparationService.prepare(consultationId);
        }
        consultationLogService.logEnter(consultation);

        if (authService.getAuthenticateUserId().equals(consultation.getDoctor().getId())) {
            payoutService.initiatePayout(consultation);
        }

        log.info("Returning call for consultation: {}", consultationId);
        return new CallResponse(consultationId, accessToken);
    }

    private @NonNull Consultation getConsultation(UUID consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));
        validatePermission(consultation);
        return consultation;
    }

    private void validatePermission(Consultation consultation) {
    }
    public void leaveCall(UUID consultationId) {
        log.info("Leaving call for consultation: {}", consultationId);
        Consultation consultation = getConsultation(consultationId);
        consultationLogService.logLeave(consultation);
    }
}
