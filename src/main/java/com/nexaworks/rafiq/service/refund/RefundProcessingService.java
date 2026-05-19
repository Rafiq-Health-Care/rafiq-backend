package com.nexaworks.rafiq.service.refund;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.RefundRequest;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.entities.enums.RefundStatus;
import com.nexaworks.rafiq.exception.custom.payment.RefundNotFoundException;
import com.nexaworks.rafiq.rabbit.manager.RefundEventManager;
import com.nexaworks.rafiq.repository.RefundRepository;
import com.nexaworks.rafiq.service.payment.PaymentService;
import com.nexaworks.rafiq.utils.TransactionUtils;
import com.stripe.exception.StripeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundProcessingService implements IRefundProcessingService {
    private final RefundRepository refundRepository;
    private final PaymentService paymentService;
    private final RefundEventManager refundEventManager;
    private final TransactionUtils transactionUtils;

    @Override
    @Transactional
    public void beginProcessing(UUID refundId) throws StripeException {
        log.info("Beginning processing for refund {}", refundId);
        RefundRequest refund = refundRepository.findById(refundId).orElseThrow(
                () -> new RefundNotFoundException("Refund not found with id: " + refundId));
        log.info("Refund {} is now in PROCESSING state", refundId);
        refund.setStatus(RefundStatus.PROCESSING);
        String stripeRefundId = paymentService.processRefund(refund.getPayment());
        refund.setStripeRefundId(stripeRefundId);
        refundRepository.save(refund);

    }

    @Override
    @Transactional
    public void markSucceeded(String refundId) {
        log.info("Marking refund {} as succeeded", refundId);
        RefundRequest refund = getRefundRequest(refundId);
        refund.setStatus(RefundStatus.COMPLETED);
        refund.getPayment().setStatus(PaymentStatus.REFUNDED);
        refundRepository.save(refund);
        log.info("Refund {} completed — payment {} marked as REFUNDED", refundId,
                refund.getPayment().getId());
        transactionUtils.afterCommit(() -> refundEventManager.publishRefundSucceededNotification(
                refund.getId(), refund.getPatient().getNotificationToken(), refund.getAmount()));
    }

    @Override
    @Transactional
    public void markFailed(String refundId) {
        log.info("Marking refund {} as failed", refundId);
        RefundRequest refund = getRefundRequest(refundId);
        refund.setStatus(RefundStatus.FAILED);
        refundRepository.save(refund);
        log.info("Refund {} marked as FAILED", refundId);
        transactionUtils.afterCommit(() -> refundEventManager.publishRefundFailedNotification(
                refund.getId(), refund.getPatient().getNotificationToken()));
    }

    private @NonNull RefundRequest getRefundRequest(String refundId) {
        return refundRepository.findByStripeRefundId(refundId).orElseThrow(
                () -> new RefundNotFoundException("Refund not found with id: " + refundId));
    }
}
