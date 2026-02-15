package com.nexaworks.rafiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.auth.LoginRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.service.authentication.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication endpoints for user login, logout, and token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns role-based access. Sets HTTP-only cookies for secure token storage. Account must be verified first.")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok()
                .body(authService.login(request.email(), request.password(), response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates authentication tokens and clears cookies. Required for security when user finishes session.")
    @ApiResponse(responseCode = "204", description = "Logout successful")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh authentication token", description = "Extends session without re-authentication. Uses refresh token from cookies. Call before access token expires.")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<LoginResponse> refresh(HttpServletResponse response,
            HttpServletRequest request) {
        return ResponseEntity.ok().body(authService.refresh(response, request));
    }

}
