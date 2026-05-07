package com.nexaworks.rafiq.service.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.exception.custom.PaymentProviderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = PaymentProviderException.class)
    public String process(Consultation consultation, Patient currentUser,
            PaymentProvider provider) {

        return "payment processed";
    }
}
