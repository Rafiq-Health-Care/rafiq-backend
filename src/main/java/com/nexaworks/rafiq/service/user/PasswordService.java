package com.nexaworks.rafiq.service.user;

import com.nexaworks.rafiq.dto.request.user.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;

public interface PasswordService {
    void forgetPassword(ForgetPasswordRequest forgetPasswordRequest);
    void changePassword(ChangePasswordRequest changePasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
