package com.nexaworks.rafiq.service.payment;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.stripe.exception.StripeException;

public interface IPaymentTrackingService {
    void check(UUID paymentId) throws StripeException;
    void update(String intentId, PaymentStatus status);

}
