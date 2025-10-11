package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.UserRegistrationRequest;
import com.nexaworks.rafiq.dto.request.VerificationRequest;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/register/patient")
    public ResponseEntity<Void> registerPatient(@RequestBody @Valid UserRegistrationRequest request){
        userService.registerPatient(UserMapper.toUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping(value = "/register/doctor",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> registerDoctor(@RequestPart("doctorData")@Valid DoctorRegistrationRequest request,
                                               @RequestPart(value = "nationalId",required = false) MultipartFile nationalId) throws IOException {
        userService.registerDoctor(request,nationalId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("/verification")
    public ResponseEntity<LoginResponse> verification(@RequestBody @Valid VerificationRequest request){

        return ResponseEntity.ok().body(userService.verifyOtp(request.email(),request.otp()));

    }
    @PostMapping("/new-otp")
    public ResponseEntity<Void> newOtp(@RequestBody ForgetPasswordRequest forgetPasswordRequest){
        userService.getNewOtp(forgetPasswordRequest.email());
        return ResponseEntity.ok().build();
    }
}
