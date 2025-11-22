package com.nexaworks.rafiq.dto.request.group;

import java.util.Optional;
import javax.validation.constraints.Max;

import com.nexaworks.rafiq.enums.Color;

public record UpdateGroupRequest(@Max(50) Optional<String> name,
        @Max(200) Optional<String> description, Optional<Color> color) {
}
