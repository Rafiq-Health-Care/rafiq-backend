package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.service.payment.IPaymentTrackingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobRunrPaymentScheduler implements PaymentScheduler {
    private final IPaymentTrackingService paymentService;

    @Override
    public void schedulePaymentTimeout(UUID paymentId) {
        BackgroundJob.schedule(paymentId, LocalDateTime.now().plusMinutes(10),
                () -> paymentService.check(paymentId));
    }

    @Override
    public void deleteJob(UUID id) {
        BackgroundJob.delete(id);
    }

}
