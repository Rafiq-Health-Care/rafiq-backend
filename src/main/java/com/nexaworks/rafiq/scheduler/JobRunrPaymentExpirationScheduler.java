package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.service.payment.IPaymentTrackingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobRunrPaymentExpirationScheduler implements PaymentScheduler {
    private final IPaymentTrackingService paymentTrackingService;
    @Override
    public void schedulePaymentExpiration(UUID paymentId) {
        log.info("Scheduling payment expiration for payment: {}", paymentId);
        BackgroundJob.schedule(paymentId, LocalDateTime.now().plusMinutes(10),
                () -> paymentTrackingService.check(paymentId));

    }

    @Override
    public void reschedule(UUID paymentId) {
        log.info("Rescheduling payment with ID {}", paymentId);
        BackgroundJob.delete(paymentId);
        schedulePaymentExpiration(paymentId);
    }
}
