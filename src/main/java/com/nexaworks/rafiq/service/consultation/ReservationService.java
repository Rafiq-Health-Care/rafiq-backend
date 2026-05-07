package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.PaymentProvider;

public interface ReservationService {
    public String reserve(UUID id, PaymentProvider provider);
}
