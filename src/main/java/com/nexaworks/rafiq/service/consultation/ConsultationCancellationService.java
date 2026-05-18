package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

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
import com.nexaworks.rafiq.exception.custom.ConsultationException;
import com.nexaworks.rafiq.exception.custom.ConsultationNotFoundException;
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
        Consultation consultation = consultationRepository.findConsultationById(id)
                .orElseThrow(() -> new ConsultationNotFoundException("Consultation not found"));

        User currentUser = authService.getAuthenticateUser();
        if (consultation.getStatus().isTerminal()) {
            throw new ConsultationException("Consultation is already completed");
        }

        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && !consultation.getPatient().getId().equals(currentUser.getId())) {
            throw new ConsultationException("You are not authorized to cancel this consultation");
        }

        boolean cancelledByPatient = cancelBookedConsultation(reason, consultation, currentUser);
        UUID refundId = refundService.refund(consultation, !cancelledByPatient);

        log.info("Consultation cancelled {} by {}", consultation.getId(), currentUser.getEmail());

        transactionUtils.afterCommit(() -> {
            eventManager.publishRefundRequestEvent(refundId);
            if (cancelledByPatient) {
                messagingTemplate.convertAndSend("/topic/consultation",
                        new ConsultationCancelled(id, ConsultationStatus.AVAILABLE));
                notificationManager.sendPatientCancelledEvent(consultation);
            } else {
                notificationManager.sendDoctorCancelledEvent(consultation);
            }
        });
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
