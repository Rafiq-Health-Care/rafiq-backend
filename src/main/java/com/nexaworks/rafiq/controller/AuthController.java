package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.request.*;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;
import com.nexaworks.rafiq.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/forget-password")
    public ResponseEntity<Void> forgetPassword(@RequestBody @Valid ForgetPasswordRequest forgetPasswordRequest){
        authService.forgetPassword(forgetPasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyOtpResponse> verify(@RequestBody @Valid VerifyOtpRequest verifyOtpRequest){
       return ResponseEntity.ok().body(authService.verifyOtp(verifyOtpRequest));
    }
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        authService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest){
        authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response){
        return ResponseEntity.ok().body(authService.login(request.email(),request.password(),response));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest request){
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody @Valid RefreshRequest request,HttpServletResponse response){
        return ResponseEntity.ok().body(authService.refresh(request,response));
    }


}
