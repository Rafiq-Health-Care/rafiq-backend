package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.ForgetPasswordRequest;
import com.nexaworks.rafiq.service.AuthService;
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
}
