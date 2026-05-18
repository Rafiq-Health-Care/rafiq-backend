package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.event.ConsultationCancelled;
import com.nexaworks.rafiq.entities.CancellationLog;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.auth.AuthorizationException;
import com.nexaworks.rafiq.exception.custom.consultation.CanNotCancelConsultation;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationNotFoundException;
import com.nexaworks.rafiq.rabbit.manager.ConsultationNotificationManager;
import com.nexaworks.rafiq.rabbit.manager.RefundEventManager;
import com.nexaworks.rafiq.repository.CancellationLogRepository;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.refund.IRefundService;
import com.nexaworks.rafiq.utils.TransactionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationCancellationService implements IConsultationCancellationService {
    private final ConsultationRepository consultationRepository;
    private final CancellationLogRepository cancellationLogRepository;
    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;
    private final IRefundService refundService;
    private final ConsultationNotificationManager notificationManager;
    private final RefundEventManager eventManager;
    private final TransactionUtils transactionUtils;
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(retryFor = {
            PessimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void cancel(UUID id, String reason) {
        User currentUser = authService.getAuthenticateUser();

        Consultation consultation = validateAndGetConsultation(id, currentUser);

        if (consultation.getStatus().equals(ConsultationStatus.PENDING)) {
            boolean cancelledByPatient = cancelBookedConsultation(reason, consultation,
                    currentUser);
            consultationRepository.save(consultation);
            log.info("Consultation cancelled {} by {}", consultation.getId(),
                    currentUser.getEmail());
            if (cancelledByPatient) {
                transactionUtils.afterCommit(() -> {
                    notificationManager.sendDoctorCancelledEvent(consultation);
                });
            }
            return;
        }

        boolean cancelledByPatient = cancelBookedConsultation(reason, consultation, currentUser);

        UUID refundId = refundService.refund(consultation, !cancelledByPatient);

        log.info("Consultation cancelled {} by {}", consultation.getId(), currentUser.getEmail());

        transactionUtils.afterCommit(() -> {
            eventManager.publishRefundRequestEvent(refundId);
            notify(id, cancelledByPatient, consultation);
        });
    }

    private @NonNull Consultation validateAndGetConsultation(UUID id, User currentUser) {
        Consultation consultation = consultationRepository.findConsultationById(id)
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));

        if (consultation.getStatus().equals(ConsultationStatus.COMPLETED)
                || consultation.getStatus().equals(ConsultationStatus.CANCELLED)) {
            throw new CanNotCancelConsultation("Consultation is already completed");
        }

        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && !consultation.getPatient().getId().equals(currentUser.getId())) {
            throw new AuthorizationException("You are not authorized to cancel this consultation");
        }
        return consultation;
    }

    private void notify(UUID id, boolean cancelledByPatient, Consultation consultation) {
        messagingTemplate.convertAndSend("/topic/consultation",
                new ConsultationCancelled(id, SlotStatus.AVAILABLE));
        if (cancelledByPatient) {
            notificationManager.sendPatientCancelledEvent(consultation);
        } else {
            notificationManager.sendDoctorCancelledEvent(consultation);
        }
    }

    private boolean cancelBookedConsultation(String reason, Consultation consultation,
            User currentUser) {
        CancellationLog cancellationLog = CancellationLog.builder().consultation(consultation)
                .cancelledBy(currentUser).reason(reason).build();

        cancellationLogRepository.save(cancellationLog);

        boolean cancelledByPatient = currentUser.getId().equals(consultation.getPatient().getId());
        consultation.setStatus(ConsultationStatus.CANCELLED);

        consultation.setCancellationLog(cancellationLog);
        consultation.getSlot().setStatus(SlotStatus.AVAILABLE);
        consultationRepository.save(consultation);
        return cancelledByPatient;
    }

}
