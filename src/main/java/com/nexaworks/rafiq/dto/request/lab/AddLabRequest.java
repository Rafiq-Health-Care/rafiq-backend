package com.nexaworks.rafiq.dto.request.lab;

import java.util.List;

import com.nexaworks.rafiq.dto.request.address.AddAddressRequest;

import jakarta.validation.constraints.NotBlank;
@Deprecated
public record AddLabRequest(@NotBlank String name, List<AddAddressRequest> addresses) {
}
