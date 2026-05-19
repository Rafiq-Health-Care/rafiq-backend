package com.nexaworks.rafiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.auth.LoginRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.service.authentication.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "User login", description = "Authenticates the user and sets HTTP-only authentication cookies.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account not verified")})
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok()
                .body(authService.login(request.email(), request.password(), response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Clears authentication cookies and invalidates the current session.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh authentication token", description = "Generates a new access token using the refresh token stored in cookies.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")})
    public ResponseEntity<LoginResponse> refresh(HttpServletResponse response,
            HttpServletRequest request) {
        return ResponseEntity.ok().body(authService.refresh(response, request));
    }

}
