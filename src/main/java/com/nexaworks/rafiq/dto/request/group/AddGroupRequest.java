package com.nexaworks.rafiq.dto.request.group;

import jakarta.validation.constraints.NotNull;

public record AddGroupRequest(@NotNull String name, String description, String color) {

}
