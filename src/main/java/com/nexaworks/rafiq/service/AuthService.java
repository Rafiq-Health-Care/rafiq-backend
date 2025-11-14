package com.nexaworks.rafiq.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.nexaworks.rafiq.dto.request.*;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {
    void forgetPassword(@Valid ForgetPasswordRequest forgetPasswordRequest);

    VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest verifyOtpRequest);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest);

    void resetPassword(@Valid ResetPasswordRequest resetPasswordRequest);

    LoginResponse login(@NotBlank String email, @NotBlank String password,
            HttpServletResponse response);

    LoginResponse refresh(HttpServletResponse response, HttpServletRequest request);

    void logout(@Valid LogoutRequest request, HttpServletResponse response);

    LoginResponse oAuth2(@NotBlank String idToken, HttpServletResponse response)
            throws GeneralSecurityException, IOException;
}
