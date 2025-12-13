package com.nexaworks.rafiq.fileManagment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "File metadata response containing details about uploaded file")
public record FileResponse(
        @Schema(description = "Name of the uploaded file", example = "lab-results-2024.pdf") String fileName,

        @Schema(description = "MIME type of the file", example = "application/pdf") String fileType,

        @Schema(description = "File size in bytes", example = "245632") long size,

        @Schema(description = "Cloudinary URL where the file is stored", example = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.pdf") String fileUrl) {
}
