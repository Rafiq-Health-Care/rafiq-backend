package com.nexaworks.rafiq.service.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.nexaworks.rafiq.service.refund.IRefundProcessingService;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeHookService {
    private final String stripeWebhookSecret;
    private final IPaymentTrackingService paymentTrackingService;
    private final IRefundProcessingService refundProcessingService;

    public StripeHookService(@Value("${stripe.webhook-secret}") String stripeWebhookSecret,
            IPaymentTrackingService paymentTrackingService,
            IRefundProcessingService refundProcessingService) {
        this.stripeWebhookSecret = stripeWebhookSecret;
        this.paymentTrackingService = paymentTrackingService;
        this.refundProcessingService = refundProcessingService;
    }

    public void handle(String payload, String sigHeader) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

        switch (event.getType()) {

            case "payment_intent.succeeded" : {

                PaymentIntent intent = extractPaymentIntent(event);

                String paymentIntentId = intent.getId();

                paymentTrackingService.update(paymentIntentId, PaymentStatus.SUCCEEDED);
                break;
            }

            case "payment_intent.payment_failed" : {
                PaymentIntent intent = extractPaymentIntent(event);
                paymentTrackingService.update(intent.getId(), PaymentStatus.FAILED);
                break;
            }
            case "refund.updated" : {
                Refund refund = extractRefund(event);
                String status = refund.getStatus();
                switch (status) {
                    case "succeeded" -> refundProcessingService.markSucceeded(refund.getId());
                    case "failed" -> refundProcessingService.markFailed(refund.getId());
                }
            }

            default :
        }
    }

    private Refund extractRefund(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;

        try {
            if (deserializer.getObject().isPresent()) {
                stripeObject = deserializer.getObject().get();
            } else {
                log.warn(
                        "Stripe API version mismatch (eventApiVersion={}, libraryApiVersion={}), "
                                + "falling back to unsafe deserialization for event type={}",
                        event.getApiVersion(), Stripe.API_VERSION, event.getType());
                stripeObject = deserializer.deserializeUnsafe();
            }
        } catch (EventDataObjectDeserializationException e) {
            log.error(
                    "Failed to deserialize Stripe webhook object "
                            + "(type={}, eventApiVersion={}, libraryApiVersion={}): {}",
                    event.getType(), event.getApiVersion(), Stripe.API_VERSION, e.getMessage());
            throw new RuntimeException("Failed to parse Stripe event", e);
        }

        if (!(stripeObject instanceof Refund refund)) {
            throw new IllegalStateException(
                    "Expected Refund in webhook, got " + stripeObject.getClass().getName());
        }

        return refund;
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        try {
            stripeObject = deserializer.getObject().orElse(null);
            if (stripeObject == null) {
                stripeObject = deserializer.deserializeUnsafe();
            }
        } catch (EventDataObjectDeserializationException e) {
            log.error(
                    "Failed to deserialize Stripe webhook object (type={}, eventApiVersion={}, libraryApiVersion={}): {}",
                    event.getType(), event.getApiVersion(), Stripe.API_VERSION, e.getMessage());
            throw new RuntimeException("Failed to parse Stripe event", e);
        }
        if (!(stripeObject instanceof PaymentIntent intent)) {
            throw new IllegalStateException(
                    "Expected PaymentIntent in webhook, got " + stripeObject.getClass().getName());
        }
        return intent;
    }
}
