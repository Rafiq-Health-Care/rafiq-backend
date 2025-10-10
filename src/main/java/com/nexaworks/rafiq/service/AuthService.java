package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.VerifyOtpRequest;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;
import jakarta.validation.Valid;

public interface AuthService {
    void forgetPassword(@Valid ForgetPasswordRequest forgetPasswordRequest);

    VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest verifyOtpRequest);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest);
}
