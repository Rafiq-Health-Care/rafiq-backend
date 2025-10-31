package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.ResetPasswordRequest;

import com.nexaworks.rafiq.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.entities.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface UserService {
    public Optional<User> findByEmail(String email);

    void changePassword(User user, @NotBlank @Size(min = 8,max = 20) String s);

    void updatePassword(User user, ResetPasswordRequest resetPasswordRequest);
    void registerPatient(User user);

    void registerDoctor(@Valid DoctorRegistrationRequest request, MultipartFile nationalId) throws IOException;

    LoginResponse verifyOtp(@NotBlank @Email String email, @NotBlank String otp, HttpServletResponse response);

    void getNewOtp(String email);

    User getUser();

    User addUser(String email, String firstName, String lastName);
}
