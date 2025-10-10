package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.request.UserRegistrationRequest;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/register/doctor")
    public ResponseEntity<Void> registerDoctor(@RequestBody @Valid DoctorRegistrationRequest request){
        userService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
