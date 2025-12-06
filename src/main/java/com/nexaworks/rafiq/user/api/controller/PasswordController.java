package com.nexaworks.rafiq.user.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.user.api.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.ResetPasswordRequest;
import com.nexaworks.rafiq.user.service.PasswordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/password")
@Tag(name = "Password Management")
public class PasswordController {
    private final PasswordService passwordService;
    @PostMapping("/forget-password")
    @Operation(summary = "Request password reset", description = "Initiates password recovery flow. Sends OTP to email for secure password reset without requiring current password.")
    public ResponseEntity<Void> forgetPassword(
            @RequestBody @Valid ForgetPasswordRequest forgetPasswordRequest) {
        passwordService.forgetPassword(forgetPasswordRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password with OTP", description = "Completes password reset using OTP from forget-password flow. No authentication required as OTP serves as proof of email ownership.")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        passwordService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password (authenticated)", description = "Allows logged-in users to change password by verifying current password. Use for proactive password updates or security rotations.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordRequest resetPasswordRequest,
            Authentication authentication) {
        passwordService.resetPassword(resetPasswordRequest, (UUID) authentication.getPrincipal());
        return ResponseEntity.noContent().build();
    }
}
