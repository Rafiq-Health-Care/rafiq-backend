package com.nexaworks.rafiq.service.consultation;

import com.nexaworks.rafiq.entities.enums.PaymentProvider;

import java.util.UUID;

public interface ReservationService {
    public String reserve(UUID id, PaymentProvider provider);
}
