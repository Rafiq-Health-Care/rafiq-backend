package com.nexaworks.rafiq.shared.exception.model;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Validation error response")
public class ValidationErrorResponse {
    @Schema
    private int status;
    @Schema
    private String error;
    @Schema
    private String message;
    @Schema
    private LocalDateTime timestamp;
    @Schema
    private Map<String, String> validationErrors;
}
