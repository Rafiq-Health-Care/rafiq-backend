package com.nexaworks.rafiq.user.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.user.api.dto.request.LoginRequest;
import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.service.AuthService;

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

}
