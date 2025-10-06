package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.ResetPasswordRequest;
import com.nexaworks.rafiq.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public interface UserService {
    public Optional<User> findByEmail(String email);

    void changePassword(User user, @NotBlank @Size(min = 8,max = 20) String s);

    void updatePassword(User user, ResetPasswordRequest resetPasswordRequest);
}
