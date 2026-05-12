package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Payment findByPaymentIntentId(String paymentIntentId);
}
