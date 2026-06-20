package com.nexaworks.rafiq.service.doctor;

import com.stripe.exception.StripeException;

public interface IAccountManagement {
    String createAccount() throws StripeException;
}
