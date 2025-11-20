package com.nexaworks.rafiq.dto.request.user;

import jakarta.validation.constraints.*;

public record UserRegistrationRequest(@NotBlank @Email String email, @NotBlank
// At least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character") String password,
        @NotBlank String firstName, String lastName,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format") String phone,
        @Min(1) @Max(100) int age,
        @NotBlank @Pattern(regexp = "^(male|female)$", message = "Gender must be either 'male' or 'female'") String gender) {
}
