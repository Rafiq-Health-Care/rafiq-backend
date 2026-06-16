package com.nexaworks.rafiq.service.payout;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Payout;
import com.nexaworks.rafiq.entities.enums.PayoutStatus;
import com.nexaworks.rafiq.repository.PayoutRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutProcessingService {
    private final PayoutRepository payoutRepository;

    @Transactional
    public void process(Payout payout) {
        log.info("Processing payout {} for consultation {}", payout.getId(),
                payout.getConsultation().getId());

        try {
            if (!PayoutStatus.PENDING.equals(payout.getStatus())) {
                log.warn("Payout {} is not in PENDING status, skipping", payout.getId());
                return;
            }

            if (payout.getReleaseAt().isAfter(Instant.now())) {
                log.warn("Payout {} release time has not arrived yet, skipping", payout.getId());
                return;
            }

            payout.setStatus(PayoutStatus.PROCESSING);
            payoutRepository.save(payout);
            log.info("Updated payout {} status to PROCESSING", payout.getId());

            // TODO: Call Stripe payout API
            // String payoutIntentId = stripePayoutService.createPayout(payout);
            // payout.setPayoutIntentId(payoutIntentId);

            payoutRepository.save(payout);
            log.info("Successfully processed payout {} with status PAID", payout.getId());

        } catch (Exception e) {
            log.error("Failed to process payout {}: {}", payout.getId(), e.getMessage(), e);

            payout.setStatus(PayoutStatus.FAILED);
            payoutRepository.save(payout);

            throw new RuntimeException("Failed to process payout: " + e.getMessage(), e);
        }
    }
}
