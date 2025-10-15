package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AddLabRequest(@NotBlank String name, List<AddAddressRequest> addresses) {

}
