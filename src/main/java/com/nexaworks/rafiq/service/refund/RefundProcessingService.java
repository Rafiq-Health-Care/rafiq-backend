package com.nexaworks.rafiq.service.refund;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.RefundRequest;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.entities.enums.RefundStatus;
import com.nexaworks.rafiq.exception.custom.payment.RefundNotFoundException;
import com.nexaworks.rafiq.repository.RefundRepository;
import com.nexaworks.rafiq.service.payment.PaymentService;
import com.stripe.exception.StripeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundProcessingService implements IRefundProcessingService {
    private final RefundRepository refundRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public void beginProcessing(UUID refundId) throws StripeException {
        log.info("Beginning processing for refund {}", refundId);
        updateRefundStatus(refundId, RefundStatus.PROCESSING);
        log.info("Refund {} is now in PROCESSING state", refundId);
        RefundRequest refund = getRefundRequest(refundId);
        String stripeRefundId = paymentService.processRefund(refund.getPayment());
        refund.setStripeRefundId(stripeRefundId);
        refundRepository.save(refund);

    }

    @Override
    @Transactional
    public void markSucceeded(UUID refundId) {
        log.info("Marking refund {} as succeeded", refundId);
        RefundRequest refund = getRefundRequest(refundId);
        refund.setStatus(RefundStatus.COMPLETED);
        refund.getPayment().setStatus(PaymentStatus.REFUNDED);
        refundRepository.save(refund);
        log.info("Refund {} completed — payment {} marked as REFUNDED", refundId,
                refund.getPayment().getId());
    }

    @Override
    @Transactional
    public void markFailed(UUID refundId) {
        log.info("Marking refund {} as failed", refundId);
        updateRefundStatus(refundId, RefundStatus.FAILED);
        log.info("Refund {} marked as FAILED", refundId);
    }

    private void updateRefundStatus(UUID refundId, RefundStatus status) {
        RefundRequest refund = getRefundRequest(refundId);
        refund.setStatus(status);
        refundRepository.save(refund);
    }

    private @NonNull RefundRequest getRefundRequest(UUID refundId) {
        return refundRepository.findById(refundId).orElseThrow(
                () -> new RefundNotFoundException("Refund not found with id: " + refundId));
    }
}
