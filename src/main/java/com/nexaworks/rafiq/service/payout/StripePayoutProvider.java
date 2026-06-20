package com.nexaworks.rafiq.service.payout;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.response.payment.PayoutResponse;
import com.nexaworks.rafiq.entities.enums.PayoutStatus;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.ApiException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.stripe.param.TransferCreateParams;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StripePayoutProvider implements PayoutProvider {
    @Override
    @Retryable(retryFor = {RateLimitException.class, ApiConnectionException.class,
            ApiException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    public PayoutResponse payout(BigDecimal amount, String accountNumber, String payoutReference)
            throws StripeException {
        long amountInCents = amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();

        TransferCreateParams params = TransferCreateParams.builder().setAmount(amountInCents)
                .setCurrency("usd").setDestination(accountNumber)
                .putMetadata("payout_reference", payoutReference).build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("payout-" + payoutReference).build();

        try {
            Transfer transfer = Transfer.create(params, options);
            return new PayoutResponse(PayoutStatus.PAID, transfer.getId(), null);

        } catch (RateLimitException | ApiConnectionException | ApiException e) {
            log.warn("Transient Stripe error for payout {}: {}", payoutReference, e.getMessage());
            throw e;

        } catch (StripeException e) {

            log.error("Permanent Stripe payout failure for payout {}: {}", payoutReference,
                    e.getMessage());
            return new PayoutResponse(PayoutStatus.FAILED, null, e.getMessage());
        }
    }

    @Recover
    public PayoutResponse recover(StripeException e, BigDecimal amount, String accountNumber,
            String payoutReference) {
        log.error("Payout {} failed after all retries: {}", payoutReference, e.getMessage());
        return new PayoutResponse(PayoutStatus.FAILED, null, e.getMessage());

    }
}
