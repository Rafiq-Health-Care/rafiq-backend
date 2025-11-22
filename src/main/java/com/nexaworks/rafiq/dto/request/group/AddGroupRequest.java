package com.nexaworks.rafiq.dto.request.group;

import java.util.Optional;

import com.nexaworks.rafiq.enums.Color;

import jakarta.validation.constraints.NotNull;

public record AddGroupRequest(@NotNull String name, Optional<String> description,
        Optional<Color> color) {

}
