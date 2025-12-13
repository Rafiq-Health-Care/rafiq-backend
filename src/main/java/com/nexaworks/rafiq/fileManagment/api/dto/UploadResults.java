package com.nexaworks.rafiq.fileManagment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Upload results")
public record UploadResults(@Schema String url, @Schema String publicId) {
}
