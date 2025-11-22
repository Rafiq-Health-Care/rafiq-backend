package com.nexaworks.rafiq.dto.request.group;

import javax.validation.constraints.Max;

import com.nexaworks.rafiq.enums.Color;

public record UpdateGroupRequest(@Max(50) String name, @Max(200) String description, Color color) {
}
