package com.nexaworks.rafiq.service.doctor;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountManagement implements IAccountManagement {
    public static final String APP_REFRESH_STRIPE_ACCOUNT = "https://rafiq-consultation-app.vercel.app/refresh-stripe-account";
    public static final String ACCOUNT_LINK_SUCCESS = "https://rafiq-consultation-app.vercel.app/stripe-account-link-success";
    private final AuthService authService;
    @Override
    public String createAccount() throws StripeException {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();

        AccountCreateParams accountCreateParams = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS).setCountry("us")
                .setEmail(doctor.getEmail())
                .setCapabilities(AccountCreateParams.Capabilities.builder()
                        .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                .setRequested(true).build())
                        .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                .setRequested(true).build())
                        .build())
                .build();
        Account account = Account.create(accountCreateParams);
        log.info("Account created: {}", doctor.getEmail());
        doctor.setStripeCustomerId(account.getId());

        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(account.getId()).setRefreshUrl(APP_REFRESH_STRIPE_ACCOUNT)
                .setReturnUrl(ACCOUNT_LINK_SUCCESS)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING).build();

        return AccountLink.create(params).getUrl();
    }
}
