package com.nexaworks.rafiq.service.payout;

import java.math.BigDecimal;

import com.nexaworks.rafiq.dto.response.payment.PayoutResponse;
import com.stripe.exception.StripeException;

public interface PayoutProvider {
    PayoutResponse payout(BigDecimal amount, String accountNumber, String payoutReference)
            throws StripeException;
}
