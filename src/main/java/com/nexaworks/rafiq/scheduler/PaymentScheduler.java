package com.nexaworks.rafiq.scheduler;

import java.util.UUID;

public interface PaymentScheduler {
    void schedulePaymentExpiration(UUID paymentId);

    void reschedule(UUID paymentId);
}
