package com.nexaworks.rafiq.service.refund;

import java.util.UUID;

import com.stripe.exception.StripeException;

public interface IRefundProcessingService {
    void beginProcessing(UUID refundId) throws StripeException;
    void markSucceeded(String refundId);
    void markFailed(String refundId);
}
