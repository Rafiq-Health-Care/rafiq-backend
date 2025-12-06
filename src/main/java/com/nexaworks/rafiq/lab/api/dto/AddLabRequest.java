package com.nexaworks.rafiq.lab.api.dto;

import java.util.List;

import com.nexaworks.rafiq.user.api.dto.request.AddAddressRequest;

import jakarta.validation.constraints.NotBlank;

public record AddLabRequest(@NotBlank String name, List<AddAddressRequest> addresses) {
}
