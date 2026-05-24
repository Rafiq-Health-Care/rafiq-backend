package com.nexaworks.rafiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.user.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.service.user.PasswordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/password")
@Tag(name = "Password Management", description = "Endpoints for password reset and change flows")
public class PasswordController {
    private final PasswordService passwordService;
    @PostMapping("/forget-password")
    @Operation(summary = "Request password reset", description = "Initiates password recovery. Sends OTP to email for secure reset.")
    @ApiResponse(responseCode = "200", description = "OTP sent successfully")
    public ResponseEntity<Void> forgetPassword(
            @RequestBody @Valid ForgetPasswordRequest forgetPasswordRequest) {
        passwordService.forgetPassword(forgetPasswordRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-password")
    @Operation(summary = "Change password ", description = "Completes password reset from forget-password flow.")
    @ApiResponse(responseCode = "204", description = "Password changed successfully")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        passwordService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reset-password")
    @Operation(summary = "Reset password (authenticated)", description = "Allows logged-in users to change password by verifying current password.")
    @ApiResponse(responseCode = "204", description = "Password reset successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        passwordService.resetPassword(resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }
}
