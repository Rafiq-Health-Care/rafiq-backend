package com.nexaworks.rafiq.service.payment;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Payment;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.exception.custom.PaymentException;
import com.nexaworks.rafiq.repository.PaymentRepository;
import com.nexaworks.rafiq.service.consultation.ConsultationService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTrackingService implements IPaymentTrackingService {
    private final PaymentRepository paymentRepository;
    private final ConsultationService consultationService;

    // TODO send notification to user
    @Override
    @Transactional
    public void check(UUID paymentId) throws StripeException {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return;
        }
        if (payment.getStatus() == PaymentStatus.PENDING) {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getPaymentIntentId());
            if (paymentIntent.getStatus().equals("succeeded")) {
                update(payment.getPaymentIntentId(), PaymentStatus.SUCCEEDED);
                return;
            }
            paymentIntent.cancel();
            consultationService.update(payment.getConsultation().getId(),
                    ConsultationStatus.AVAILABLE);
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            log.info("Payment {} is cancelled", paymentId);
        }
    }

    private @NonNull Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
    }

    @Override
    @Transactional
    public void update(String intentId, PaymentStatus status) {
        Payment payment = paymentRepository.findByPaymentIntentId(intentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
        payment.setStatus(status);
        paymentRepository.save(payment);
        if (status != PaymentStatus.SUCCEEDED) {
            consultationService.update(payment.getConsultation().getId(),
                    ConsultationStatus.AVAILABLE);
            return;
        }
        consultationService.update(payment.getConsultation().getId(), ConsultationStatus.CONFIRMED);
        log.info("Payment {} is updated to {}", payment.getId(), status);
    }

}
