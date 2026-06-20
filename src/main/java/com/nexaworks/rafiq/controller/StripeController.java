package com.nexaworks.rafiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.service.payment.StripeHookService;
import com.stripe.exception.SignatureVerificationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/stripe/webhook")
@Slf4j
@RequiredArgsConstructor
public class StripeController {
    private final StripeHookService stripeHookService;

    @PostMapping
    public ResponseEntity<?> handleWebhookNotification(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader)
            throws SignatureVerificationException {
        stripeHookService.handle(payload, sigHeader);
        return ResponseEntity.ok().build();

    }

}
