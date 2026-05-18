package com.nexaworks.rafiq.service.payment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.enums.PaymentProvider;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentProviderRegistry implements IPaymentProviderRegistry {
    private final Map<PaymentProvider, PaymentProviderService> paymentProviders;

    public PaymentProviderRegistry(@Qualifier("stripe") PaymentProviderService stripeService) {
        this.paymentProviders = new ConcurrentHashMap<>();
        this.paymentProviders.put(PaymentProvider.STRIPE, stripeService);
    }

    @Override
    public void register(PaymentProvider provider, PaymentProviderService service) {
        this.paymentProviders.put(provider, service);
    }

    @Override
    public PaymentProviderService getService(PaymentProvider provider) {
        return paymentProviders.get(provider);
    }

}
