package com.nexaworks.rafiq.service;


import com.nexaworks.rafiq.dto.request.*;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthService {
    void forgetPassword(@Valid ForgetPasswordRequest forgetPasswordRequest);

    VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest verifyOtpRequest);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest);


    void resetPassword(@Valid ResetPasswordRequest resetPasswordRequest);

    LoginResponse login(@NotBlank String email, @NotBlank String password, HttpServletResponse response);

    LoginResponse refresh(@Valid RefreshRequest request,HttpServletResponse response);

    void logout(@Valid LogoutRequest request,HttpServletResponse response);

    LoginResponse oAuth2(@NotBlank String idToken,HttpServletResponse response) throws GeneralSecurityException, IOException;
}
