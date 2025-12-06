package com.nexaworks.rafiq.user.service;

import java.util.UUID;

import com.nexaworks.rafiq.user.api.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ResetPasswordRequest;

public interface PasswordService {
    void forgetPassword(ForgetPasswordRequest forgetPasswordRequest);
    void changePassword(ChangePasswordRequest changePasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest, UUID userId);
}
