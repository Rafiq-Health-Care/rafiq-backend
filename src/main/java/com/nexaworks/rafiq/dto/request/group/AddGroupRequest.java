package com.nexaworks.rafiq.dto.request.group;

import com.nexaworks.rafiq.entities.enums.Color;

import jakarta.validation.constraints.NotNull;

public record AddGroupRequest(@NotNull String name, String description, Color color) {

}
