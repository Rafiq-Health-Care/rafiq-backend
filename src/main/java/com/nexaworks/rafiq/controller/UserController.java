package com.nexaworks.rafiq.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.user.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.UserRegistrationRequest;
import com.nexaworks.rafiq.dto.request.user.VerificationRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.service.user.TokenService;
import com.nexaworks.rafiq.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user registration and email verification")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    @PostMapping("/register/patient")
    @Operation(summary = "Register a new patient", description = "Creates a patient account and sends OTP for email verification. Required before accessing patient-specific features.")
    @ApiResponse(responseCode = "201", description = "Patient registered successfully, OTP sent to email")
    public ResponseEntity<Void> registerPatient(
            @Valid @RequestBody UserRegistrationRequest request) {
        userService.registerPatient(userMapper.toUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/register/doctor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register a new doctor", description = "Creates a doctor account with specialization. National ID upload is optional but recommended for verification. Account requires email verification before activation.")
    @ApiResponse(responseCode = "201", description = "Doctor registered successfully, OTP sent to email")
    public ResponseEntity<Void> registerDoctor(
            @RequestPart("doctorData") @Valid DoctorRegistrationRequest request,
            @RequestPart(value = "nationalId", required = false) MultipartFile nationalId)
            throws IOException {
        userService.registerDoctor(userMapper.toDoctor(request.user()), nationalId,
                request.specialization(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verification")
    @Operation(summary = "Verify user email with OTP", description = "Completes account activation after registration. Returns authentication tokens and user role. Must be called before first login.")
    @ApiResponse(responseCode = "200", description = "Email verified successfully", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<LoginResponse> verification(
            @RequestBody @Valid VerificationRequest request, HttpServletResponse response) {

        return ResponseEntity.ok()
                .body(userService.verifyUserEmail(request.email(), request.otp(), response));
    }

    @PostMapping("/new-otp")
    @Operation(summary = "Request a new OTP", description = "Resends OTP when original expires or is lost. Use for email verification or password reset flows.")
    @ApiResponse(responseCode = "200", description = "New OTP sent successfully")
    public ResponseEntity<Void> newOtp(@RequestBody ForgetPasswordRequest forgetPasswordRequest) {
        tokenService.getNewOtp(forgetPasswordRequest.email());
        return ResponseEntity.ok().build();
    }
}
