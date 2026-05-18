package com.nexaworks.rafiq.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.response.payment.PaymentDto;
import com.nexaworks.rafiq.exception.custom.payment.PaymentException;
import com.nexaworks.rafiq.exception.custom.payment.PaymentProviderException;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

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
        long amountInCents = amount.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                .longValue();
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

    @Override
    @Retryable(maxAttempts = 3, retryFor = {RateLimitException.class,
            ApiConnectionException.class}, backoff = @Backoff(delay = 1000, multiplier = 3))
    public String refund(String paymentIntentId, BigDecimal amount)
            throws RateLimitException, ApiConnectionException {
        if (paymentIntentId == null)
            throw new PaymentException("PaymentIntent ID cannot be null");
        if (amount == null)
            throw new PaymentException("Amount cannot be null");

        long amountInCents = amount.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                .longValue();
        RefundCreateParams params = RefundCreateParams.builder().setPaymentIntent(paymentIntentId)
                .setAmount(amountInCents).setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();
        try {
            log.info("Refunding paymentIntentId={}", paymentIntentId);
            return Refund.create(params).getId();
        } catch (ApiConnectionException | RateLimitException e) {
            log.error("Retryable Stripe error on refund: {}", e.getMessage());
            throw e;
        } catch (StripeException e) {
            log.error("Refund failed for paymentIntentId={}: {}", paymentIntentId, e.getMessage());
            throw new PaymentProviderException("Refund failed: " + e.getMessage());
        }
    }

    @Recover
    public String recoverRefund(Exception e, String paymentIntentId, BigDecimal amount) {
        log.error("All retries exhausted for paymentIntentId={}: {}", paymentIntentId,
                e.getMessage());
        throw new PaymentProviderException("Refund failed after all attempts: " + e.getMessage());
    }

    @Recover
    public PaymentDto recoverPayment(Exception e, String consultationId, BigDecimal amount) {
        log.error("All retries exhausted for consultationId={}: {}", consultationId,
                e.getMessage());
        throw new PaymentProviderException("Payment failed after all attempts: " + e.getMessage());
    }
}
