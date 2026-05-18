package com.nexaworks.rafiq.service.refund;

import java.util.UUID;

import com.nexaworks.rafiq.entities.Consultation;

public interface IRefundService {
    UUID refund(Consultation consultation, boolean isFullRefund);
}
