package com.nexaworks.rafiq.service.payment;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Payment;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.exception.custom.PaymentException;
import com.nexaworks.rafiq.repository.PaymentRepository;
import com.nexaworks.rafiq.scheduler.PaymentScheduler;
import com.nexaworks.rafiq.service.consultation.IConsultationService;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentTrackingService implements IPaymentTrackingService {
    private final PaymentRepository paymentRepository;
    private final IConsultationService IConsultationService;
    private final PaymentScheduler paymentScheduler;

    public PaymentTrackingService(PaymentRepository paymentRepository,
            IConsultationService IConsultationService, @Lazy PaymentScheduler paymentScheduler) {
        this.paymentRepository = paymentRepository;
        this.IConsultationService = IConsultationService;
        this.paymentScheduler = paymentScheduler;
    }

    // TODO send notification to user
    @Override
    @Transactional
    public void check(UUID paymentId) throws StripeException {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() == PaymentStatus.SUCCEEDED
                || payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.CANCELLED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            return;
        }
        PaymentIntent intent = PaymentIntent.retrieve(payment.getPaymentIntentId());
        switch (intent.getStatus()) {
            case "succeeded" -> update(intent.getId(), PaymentStatus.SUCCEEDED);
            case "canceled" -> update(intent.getId(), PaymentStatus.CANCELLED);
            case "processing" -> {
                paymentScheduler.deleteJob(payment.getId());
                paymentScheduler.schedulePaymentTimeout(payment.getId());
            }
            default -> {
                try {
                    intent.cancel();
                } catch (InvalidRequestException e) {
                    PaymentIntent fresh = PaymentIntent.retrieve(intent.getId());
                    if ("succeeded".equals(fresh.getStatus())) {
                        update(fresh.getId(), PaymentStatus.SUCCEEDED);
                        return;
                    }
                }
                update(intent.getId(), PaymentStatus.CANCELLED);
            }
        }
    }

    private @NonNull Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
    }
    @Transactional
    public void update(String intentId, PaymentStatus status) {
        Payment payment = paymentRepository.findByPaymentIntentId(intentId).orElse(null);
        if (payment == null) {
            log.warn("Webhook for unknown intent {}", intentId);
            return;
        }
        if (payment.getStatus() == PaymentStatus.SUCCEEDED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Ignoring {} for terminal payment {}", status, payment.getId());
            return;
        }
        payment.setStatus(status);
        paymentRepository.save(payment);
        log.info("Payment {} is updated to {}", payment.getId(), status);

        UUID consId = payment.getConsultation().getId();
        if (status == PaymentStatus.SUCCEEDED) {
            IConsultationService.update(consId, ConsultationStatus.CONFIRMED);
        } else if (status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) {
            IConsultationService.update(consId, ConsultationStatus.AVAILABLE);
        }
    }

}
