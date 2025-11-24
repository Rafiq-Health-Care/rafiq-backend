package com.nexaworks.rafiq.dto.request.group;

import com.nexaworks.rafiq.entities.enums.Color;

import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(@Size(max = 50) String name, @Size(max = 100) String description,
        Color color) {
}
