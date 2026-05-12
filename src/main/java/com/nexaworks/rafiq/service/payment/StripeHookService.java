package com.nexaworks.rafiq.service.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.enums.PaymentStatus;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeHookService {
    private final String stripeWebhookSecret;
    private final IPaymentTrackingService paymentTrackingService;

    public StripeHookService(@Value("${stripe.webhook-secret}") String stripeWebhookSecret,
            IPaymentTrackingService paymentTrackingService) {
        this.stripeWebhookSecret = stripeWebhookSecret;
        this.paymentTrackingService = paymentTrackingService;
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

            default :
        }
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
