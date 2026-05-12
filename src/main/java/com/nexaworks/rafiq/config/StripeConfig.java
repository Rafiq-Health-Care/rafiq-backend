package com.nexaworks.rafiq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;

@Configuration
public class StripeConfig {
    @Value("${stripe.api-key}")
    private String stripeApiSecret;
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiSecret;
    }

}
