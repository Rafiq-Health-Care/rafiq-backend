package com.nexaworks.rafiq.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record AddLabRequest(@NotBlank String name, List<AddAddressRequest> addresses) {}
