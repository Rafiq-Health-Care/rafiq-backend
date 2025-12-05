package com.nexaworks.rafiq.user.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.entity.model.User;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface UserService {

    void registerPatient(User user);

    void registerDoctor(User user, MultipartFile nationalId, UUID specialization,
            String description) throws IOException;

    LoginResponse verifyUserEmail(@NotBlank @Email String email, @NotBlank String otp,
            HttpServletResponse response);

    User addUser(String email, String firstName, String lastName);


    UUID getUserId();

    Optional<User> getUser(String email, String firstName, String lastName);
}
