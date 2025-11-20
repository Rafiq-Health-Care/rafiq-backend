package com.nexaworks.rafiq.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.auth.LoginRequest;
import com.nexaworks.rafiq.dto.request.auth.OAuthRequest;
import com.nexaworks.rafiq.dto.request.user.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.VerifyOtpRequest;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;
import com.nexaworks.rafiq.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/forget-password")
    public ResponseEntity<Void> forgetPassword(
            @RequestBody @Valid ForgetPasswordRequest forgetPasswordRequest) {
        authService.forgetPassword(forgetPasswordRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyOtpResponse> verify(
            @RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
        return ResponseEntity.ok().body(authService.verifyOtp(verifyOtpRequest));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        authService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok()
                .body(authService.login(request.email(), request.password(), response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletResponse response,
            HttpServletRequest request) {
        return ResponseEntity.ok().body(authService.refresh(response, request));
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> oAuth2(@RequestBody OAuthRequest request,
            HttpServletResponse response) throws GeneralSecurityException, IOException {
        return ResponseEntity.ok().body(authService.oAuth2(request.idToken(), response));
    }
}
