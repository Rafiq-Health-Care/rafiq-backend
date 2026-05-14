package com.nexaworks.rafiq.service.refund;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Payment;
import com.nexaworks.rafiq.entities.RefundRequest;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.exception.custom.CanNotRefundException;
import com.nexaworks.rafiq.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService implements IRefundService {
    private final RefundRepository refundRepository;
    @Override
    @Transactional
    public void refund(Consultation consultation) {
        validatePayment(consultation.getPayment());
        log.info("Refunding consultation {}", consultation.getId());
        BigDecimal refundAmount = getRefundAmount(consultation.getStatus(),
                consultation.getPayment().getAmount());

        RefundRequest refundRequest = RefundRequest.builder().amount(refundAmount)
                .payment(consultation.getPayment()).consultation(consultation)
                .patient(consultation.getPatient()).build();

        refundRepository.save(refundRequest);

        log.info("Refund request saved for consultation {}", consultation.getId());
    }

    private BigDecimal getRefundAmount(ConsultationStatus status, BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(status.getRefundPercentage()));
    }

    private void validatePayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new CanNotRefundException(
                    "Cannot refund payment with status " + payment.getStatus());
        }
    }
}
