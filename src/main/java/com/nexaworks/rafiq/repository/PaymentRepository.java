package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
