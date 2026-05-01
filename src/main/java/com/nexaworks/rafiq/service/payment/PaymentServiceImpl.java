package com.nexaworks.rafiq.service.payment;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService{
    @Override
    public String process(Consultation consultation, Patient currentUser, PaymentProvider provider) {

        return "payment processed";
    }
}
