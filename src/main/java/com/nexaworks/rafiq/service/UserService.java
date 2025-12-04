package com.nexaworks.rafiq.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.User;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface UserService {
    Optional<User> findByEmail(String email);

    void registerPatient(User user);

    void registerDoctor(User user, MultipartFile nationalId, UUID specialization,
            String description) throws IOException;

    LoginResponse verifyUserEmail(@NotBlank @Email String email, @NotBlank String otp,
            HttpServletResponse response);

    void getNewOtp(String email);

    User addUser(String email, String firstName, String lastName);

    String getNotificationToken();
    UUID getUserId();

    Optional<User> getUser(String email, String firstName, String lastName);
}
