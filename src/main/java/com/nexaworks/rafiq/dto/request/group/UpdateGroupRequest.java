package com.nexaworks.rafiq.dto.request.group;

import com.nexaworks.rafiq.enums.Color;

import jakarta.validation.constraints.Max;

public record UpdateGroupRequest(@Max(50) String name, @Max(200) String description, Color color) {
}
