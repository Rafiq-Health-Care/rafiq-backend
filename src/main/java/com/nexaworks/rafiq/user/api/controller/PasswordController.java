package com.nexaworks.rafiq.user.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.user.api.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ResetPasswordRequest;
import com.nexaworks.rafiq.user.service.PasswordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/password")
public class PasswordController {
    private final PasswordService passwordService;
    @PostMapping("/forget-password")
    public ResponseEntity<Void> forgetPassword(
            @RequestBody @Valid ForgetPasswordRequest forgetPasswordRequest) {
        passwordService.forgetPassword(forgetPasswordRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        passwordService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        passwordService.resetPassword(resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }
}
