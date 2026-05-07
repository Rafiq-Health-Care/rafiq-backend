package com.nexaworks.rafiq.service.payment;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;

public interface PaymentService {
    String process(Consultation consultation, Patient currentUser, PaymentProvider provider);
}
