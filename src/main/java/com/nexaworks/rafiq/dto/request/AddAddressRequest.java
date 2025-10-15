package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddAddressRequest(@NotBlank String street,
                                @NotBlank String city,
                                @NotBlank String state,
                                @NotBlank String country,
                                @NotBlank @Pattern(regexp = "^[0-9]{5}$", message = "Invalid Egyptian postal code — must be exactly 5 digits")
                                String postalCode) {
}
