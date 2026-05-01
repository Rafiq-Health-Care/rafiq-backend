package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.PaymentJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentJobRepository extends JpaRepository<PaymentJob, UUID> {
}
