package com.nexaworks.rafiq.service.payment;

import com.nexaworks.rafiq.entities.enums.PaymentProvider;

public interface IPaymentProviderRegistry {
    void register(PaymentProvider provider, PaymentProviderService service);
    PaymentProviderService getService(PaymentProvider provider);
}
