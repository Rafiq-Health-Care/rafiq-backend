package com.nexaworks.rafiq.service.payment;

import java.math.BigDecimal;

import com.nexaworks.rafiq.dto.response.payment.PaymentDto;
import com.stripe.exception.StripeException;

public interface PaymentProviderService {
    PaymentDto pay(String consultationId, BigDecimal amount) throws StripeException;
}
