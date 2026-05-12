package com.nexaworks.rafiq.service.payment;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.response.payment.PaymentDto;
import com.nexaworks.rafiq.exception.custom.PaymentException;
import com.nexaworks.rafiq.exception.custom.PaymentProviderException;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("stripe")
@Slf4j
@RequiredArgsConstructor
public class StripeService implements PaymentProviderService {
    @Override
    @Retryable(maxAttempts = 3, retryFor = {RateLimitException.class,
            ApiConnectionException.class}, backoff = @Backoff(delay = 1000, multiplier = 3))
    public PaymentDto pay(String consultationId, BigDecimal amount)
            throws RateLimitException, ApiConnectionException {
        if (amount == null) {
            throw new PaymentException("Amount cannot be null");
        }
        if (consultationId == null) {
            throw new PaymentException("Consultation ID cannot be null");
        }
        log.info("Creating PaymentIntent for consultationId: {}", consultationId);
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents).setCurrency("usd").addPaymentMethodType("card")
                .putMetadata("consultationId", consultationId).build();
        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("PaymentIntent created: {}", paymentIntent.getId());
            return new PaymentDto(paymentIntent.getClientSecret(), paymentIntent.getId());
        } catch (ApiConnectionException | RateLimitException e) {
            log.error("Stripe API connection error: {} retrying..", e.getMessage());
            throw e;
        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", e.getMessage());
            throw new PaymentProviderException(e.getMessage());
        }
    }
    @Recover
    public PaymentDto recoverPayment(Exception e, String consultationId, BigDecimal amount) {
        log.error("All retries exhausted for consultationId={}: {}", consultationId,
                e.getMessage());
        throw new PaymentProviderException("Payment failed after all attempts: " + e.getMessage());
    }
}
