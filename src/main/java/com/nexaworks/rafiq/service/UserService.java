package com.nexaworks.rafiq.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.entities.User;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface UserService {
    Optional<User> findByEmail(String email);

    void changePassword(User user, @NotBlank @Size(min = 8, max = 20) String s);

    void updatePassword(User user, ResetPasswordRequest resetPasswordRequest);

    void registerPatient(User user);

    void registerDoctor(User user, MultipartFile nationalId, UUID specialization,
            String description) throws IOException;

    LoginResponse verifyUserEmail(@NotBlank @Email String email, @NotBlank String otp,
            HttpServletResponse response);

    void getNewOtp(String email);

    User getUser();

    User addUser(String email, String firstName, String lastName);
}
