package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.*;
import jakarta.validation.Valid;

public interface AuthService {
    void forgetPassword(@Valid ForgetPasswordRequest forgetPasswordRequest);

    VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest verifyOtpRequest);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest);

    void resetPassword(@Valid ResetPasswordRequest resetPasswordRequest);
}
