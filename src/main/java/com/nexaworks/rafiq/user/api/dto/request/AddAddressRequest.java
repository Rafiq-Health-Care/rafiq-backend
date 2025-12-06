package com.nexaworks.rafiq.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Add address request")
public record AddAddressRequest(@NotBlank @Schema String street, @NotBlank @Schema String city,
        @NotBlank @Schema String state, @NotBlank @Schema String country,
        @NotBlank @Pattern(regexp = "^[0-9]{5}$", message = "Invalid Egyptian postal code — must be exactly 5 digits") @Schema String postalCode) {
}
