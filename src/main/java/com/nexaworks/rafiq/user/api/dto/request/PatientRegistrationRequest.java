package com.nexaworks.rafiq.user.api.dto.request;

import java.time.LocalDate;

import com.nexaworks.rafiq.user.validation.annotation.ValidAge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Patient registration request")
public record PatientRegistrationRequest(@NotBlank @Email @Schema String email,
        @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character") @Schema String password,
        @NotBlank @Schema String firstName, @Schema String lastName,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format") @Schema String phone,
        @NotBlank @Pattern(regexp = "^(male|female)$", message = "Gender must be either 'male' or 'female'") @Schema(allowableValues = {
                "male", "female"}) String gender,
        @ValidAge @Schema LocalDate birthDate){
}
