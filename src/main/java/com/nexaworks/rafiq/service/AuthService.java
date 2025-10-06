package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.ForgetPasswordRequest;
import jakarta.validation.Valid;

public interface AuthService {
    void forgetPassword(@Valid ForgetPasswordRequest forgetPasswordRequest);
}
