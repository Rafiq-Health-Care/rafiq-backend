package com.nexaworks.rafiq.shared.exception.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response")
public class ErrorResponse {
    @Schema
    private int status;
    @Schema
    private String error;
    @Schema
    private String message;
    @Schema
    private LocalDateTime timestamp;
    @Schema
    private String path;
}
