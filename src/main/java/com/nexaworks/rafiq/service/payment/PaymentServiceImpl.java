package com.nexaworks.rafiq.service.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.response.payment.PaymentDto;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.Payment;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.exception.custom.PaymentException;
import com.nexaworks.rafiq.exception.custom.PaymentProviderException;
import com.nexaworks.rafiq.repository.PaymentRepository;
import com.nexaworks.rafiq.scheduler.PaymentScheduler;
import com.stripe.exception.StripeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentProviderService paymentProviderService;
    private final PaymentRepository paymentRepository;
    private final PaymentScheduler paymentScheduler;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {
            PaymentProviderException.class, PaymentException.class})
    public String process(Consultation consultation, Patient currentUser, PaymentProvider provider)
            throws StripeException {

        PaymentDto paymentDto = paymentProviderService.pay(String.valueOf(consultation.getId()),
                consultation.getDoctor().getPrice());
        Payment payment = Payment.builder().paymentIntentId(paymentDto.paymentIntentId())
                .clientSecret(paymentDto.clientSecret()).amount(consultation.getDoctor().getPrice())
                .currency("usd").patient(consultation.getPatient()).consultation(consultation)
                .build();
        paymentRepository.save(payment);
        consultation.setPayment(payment);
        paymentScheduler.schedulePaymentTimeout(payment.getId());
        return paymentDto.clientSecret();
    }
}
