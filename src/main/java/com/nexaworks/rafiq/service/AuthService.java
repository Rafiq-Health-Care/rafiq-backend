package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.VerifyOtpRequest;
import com.nexaworks.rafiq.dto.VerifyOtpResponse;
import jakarta.validation.Valid;

public interface AuthService {
    void forgetPassword(@Valid ForgetPasswordRequest forgetPasswordRequest);

    VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest verifyOtpRequest);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest);
}
