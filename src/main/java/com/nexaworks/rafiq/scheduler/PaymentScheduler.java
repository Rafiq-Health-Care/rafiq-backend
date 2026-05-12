package com.nexaworks.rafiq.scheduler;

import java.util.UUID;

public interface PaymentScheduler {
    void schedulePaymentTimeout(UUID paymentId);
}
