package com.nexaworks.rafiq.service.refund;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Payment;
import com.nexaworks.rafiq.entities.RefundRequest;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.exception.custom.payment.CanNotRefundException;
import com.nexaworks.rafiq.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService implements IRefundService {

    private final RefundRepository refundRepository;

    private static final BigDecimal REFUND_100 = BigDecimal.valueOf(1.0);
    private static final BigDecimal REFUND_80 = BigDecimal.valueOf(0.8);
    private static final BigDecimal REFUND_75 = BigDecimal.valueOf(0.75);
    private static final BigDecimal REFUND_70 = BigDecimal.valueOf(0.7);
    private static final BigDecimal REFUND_60 = BigDecimal.valueOf(0.6);
    private static final BigDecimal REFUND_50 = BigDecimal.valueOf(0.5);
    private static final BigDecimal REFUND_0 = BigDecimal.valueOf(0.0);

    @Override
    @Transactional
    public UUID refund(Consultation consultation, boolean isFullRefund) {
        validateConsultation(consultation);
        validatePayment(consultation.getPayment());

        if (refundRepository.existsByConsultation(consultation)) {
            throw new CanNotRefundException(
                    "Refund already requested for consultation " + consultation.getId());
        }

        log.info("Refunding consultation {}", consultation.getId());
        BigDecimal refundAmount;
        if (isFullRefund) {
            refundAmount = consultation.getPayment().getAmount();
        } else {

            refundAmount = getRefundAmount(consultation.getSlot().getStartTime(),
                    consultation.getPayment().getAmount());
        }

        RefundRequest refundRequest = RefundRequest.builder().amount(refundAmount)
                .payment(consultation.getPayment()).consultation(consultation)
                .patient(consultation.getPatient()).build();

        return refundRepository.save(refundRequest).getId();
    }

    private BigDecimal getRefundAmount(LocalDateTime startTime, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilStart = ChronoUnit.HOURS.between(now, startTime);
        long minutesUntilStart = ChronoUnit.MINUTES.between(now, startTime);

        BigDecimal percentage = getBigDecimal(hoursUntilStart, minutesUntilStart);

        log.info("Refund percentage: {}% for startTime: {}",
                percentage.multiply(BigDecimal.valueOf(100)), startTime);

        return amount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    }

    private static @NonNull BigDecimal getBigDecimal(long hoursUntilStart, long minutesUntilStart) {
        BigDecimal percentage;

        if (hoursUntilStart >= 24) {
            percentage = REFUND_100;
        } else if (hoursUntilStart >= 12) {
            percentage = REFUND_80;
        } else if (hoursUntilStart >= 6) {
            percentage = REFUND_75;
        } else if (hoursUntilStart >= 3) {
            percentage = REFUND_70;
        } else if (hoursUntilStart >= 1) {
            percentage = REFUND_60;
        } else if (minutesUntilStart >= 30) {
            percentage = REFUND_50;
        } else {
            percentage = REFUND_0;
        }
        return percentage;
    }

    private void validateConsultation(Consultation consultation) {
        if (consultation.getStatus() != ConsultationStatus.CANCELLED) {
            throw new CanNotRefundException(
                    "Cannot refund a non-cancelled consultation " + consultation.getId());
        }
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new CanNotRefundException("No payment found for this consultation");
        }
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new CanNotRefundException(
                    "Cannot refund payment with status " + payment.getStatus());
        }
    }
}