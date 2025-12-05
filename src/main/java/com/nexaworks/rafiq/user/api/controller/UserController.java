package com.nexaworks.rafiq.user.api.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.user.api.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.user.api.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.user.api.dto.request.PatientRegistrationRequest;
import com.nexaworks.rafiq.user.api.dto.request.VerificationRequest;
import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.mapper.UserMapper;
import com.nexaworks.rafiq.user.service.TokenService;
import com.nexaworks.rafiq.user.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    @PostMapping("/register/patient")
    public ResponseEntity<Void> registerPatient(
            @Valid @RequestBody PatientRegistrationRequest request) {
        userService.registerPatient(userMapper.toUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/register/doctor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> registerDoctor(
            @RequestPart("doctorData") @Valid DoctorRegistrationRequest request,
            @RequestPart(value = "nationalId", required = false) MultipartFile nationalId)
            throws IOException {
        userService.registerDoctor(userMapper.toDoctor(request.user()), nationalId,
                request.specialization(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verification")
    public ResponseEntity<LoginResponse> verification(
            @RequestBody @Valid VerificationRequest request, HttpServletResponse response) {

        return ResponseEntity.ok()
                .body(userService.verifyUserEmail(request.email(), request.otp(), response));
    }

    @PostMapping("/new-otp")
    public ResponseEntity<Void> newOtp(@RequestBody ForgetPasswordRequest forgetPasswordRequest) {
        tokenService.getNewOtp(forgetPasswordRequest.email());
        return ResponseEntity.ok().build();
    }
}
