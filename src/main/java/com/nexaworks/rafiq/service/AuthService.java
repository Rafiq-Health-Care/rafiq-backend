package com.nexaworks.rafiq.service;


import com.nexaworks.rafiq.dto.request.*;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {
    void forgetPassword(@Valid ForgetPasswordRequest forgetPasswordRequest);

    VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest verifyOtpRequest);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest);


    void resetPassword(@Valid ResetPasswordRequest resetPasswordRequest);

    LoginResponse login(@NotBlank String email, @NotBlank String password);

    LoginResponse refresh(@Valid RefreshRequest request);

    void logout(@Valid LogoutRequest request);

}
