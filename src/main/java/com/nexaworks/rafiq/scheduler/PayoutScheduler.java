package com.nexaworks.rafiq.scheduler;

import static org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob;

import java.time.Instant;
import java.util.List;

import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.Payout;
import com.nexaworks.rafiq.entities.enums.PayoutStatus;
import com.nexaworks.rafiq.repository.PayoutRepository;
import com.nexaworks.rafiq.service.payout.PayoutProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutScheduler {
    private final PayoutRepository payoutRepository;
    private final PayoutProcessingService payoutProcessingService;

    @EventListener(ApplicationReadyEvent.class)
    public void registerRecurringJob() {
        log.info("Registering recurring payout processing job");
        BackgroundJob.createRecurrently(aRecurringJob().withId("payout-processing-job")
                .withName("Process Eligible Payouts").withAmountOfRetries(3)
                .withCron(Cron.every15minutes()).withJobLambda(this::processEligiblePayouts));
    }

    public void processEligiblePayouts() {
        log.info("Processing eligible payouts");
        try {

            List<Payout> eligiblePayouts = payoutRepository
                    .findPayoutsReadyForProcessing(PayoutStatus.PENDING, Instant.now());

            log.info("Found {} eligible payouts for processing", eligiblePayouts.size());

            eligiblePayouts.stream().limit(10).forEach(payout -> {
                try {
                    payoutProcessingService.process(payout);
                } catch (Exception e) {
                    log.error("Error processing payout {}: {}", payout.getId(), e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error("Error in payout processing job: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
