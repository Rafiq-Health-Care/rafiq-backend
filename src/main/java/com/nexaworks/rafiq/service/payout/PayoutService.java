package com.nexaworks.rafiq.service.payout;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Payout;
import com.nexaworks.rafiq.entities.enums.PayoutStatus;
import com.nexaworks.rafiq.repository.PayoutRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutService {
    private final PayoutRepository payoutRepository;
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.20");

    @Transactional
    public void initiatePayout(Consultation consultation) {
        if (payoutRepository.findByConsultationId(consultation.getId()).isPresent()) {
            log.warn("Payout already exists for consultation: {}", consultation.getId());
            return;
        }

        BigDecimal doctorPrice = consultation.getDoctor().getPrice();
        BigDecimal netAmount = doctorPrice.multiply(BigDecimal.ONE.subtract(COMMISSION_RATE));

        Payout payout = Payout.builder().consultation(consultation).doctor(consultation.getDoctor())
                .amount(netAmount).status(PayoutStatus.PENDING)
                .releaseAt(Instant.now().plusSeconds(86400)) // 24 hours
                .build();

        Payout savedPayout = payoutRepository.save(payout);
        log.info("Created payout {} for consultation {} with amount {}", savedPayout.getId(),
                consultation.getId(), netAmount);
    }
}
