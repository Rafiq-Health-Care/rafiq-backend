package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.stripe.exception.StripeException;

public interface ReservationService {
    public String reserve(UUID id, PaymentProvider provider) throws StripeException;
}
