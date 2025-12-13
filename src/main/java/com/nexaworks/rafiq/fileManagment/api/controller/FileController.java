package com.nexaworks.rafiq.fileManagment.api.controller;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.nexaworks.rafiq.fileManagment.api.dto.FileResponse;
import com.nexaworks.rafiq.fileManagment.entity.FileMetaData;
import com.nexaworks.rafiq.fileManagment.mapper.FileMapper;
import com.nexaworks.rafiq.fileManagment.service.FileMetaDataService;
import com.nexaworks.rafiq.fileManagment.service.PdfExtractorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "FileMetaData Management")
public class FileController {
    private final PdfExtractorService pdfExtractorService;
    private final FileMetaDataService fileMetaDataService;
    private final FileMapper fileMapper;

    private UUID getUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping(value = "/extract-lab-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload and extract lab test results from PDF", description = "Uploads a PDF or image file containing lab test results. Uses AI to extract structured data from the document and automatically creates lab test records. Supports both PDF files and images (converted to PDF). Returns extracted results with file ID for tracking.")
    @ApiResponse(responseCode = "200", description = "Lab test results extracted successfully", content = @Content(schema = @Schema(implementation = String.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
            Authentication authentication)
            throws IOException, DocumentException, ExecutionException, InterruptedException {
        return ResponseEntity.ok()
                .body(pdfExtractorService.extractPdf(file, getUserId(authentication)));
    }
    @GetMapping("/{file-id}")
    @Operation(summary = "Get file by ID", description = "Retrieves metadata for a specific uploaded file. Returns file details including name, type, size, and Cloudinary URL. Only the file owner can access their uploaded files.")
    @ApiResponse(responseCode = "200", description = "File metadata retrieved successfully", content = @Content(schema = @Schema(implementation = FileResponse.class)))
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "404", description = "File not found or does not belong to user")
    @ApiResponse(responseCode = "400", description = "Invalid file ID format")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FileResponse> getFile(@PathVariable("file-id") UUID fileId,
            Authentication authentication) throws IOException {
        FileMetaData file = fileMetaDataService.getFile(fileId,
                (UUID) authentication.getPrincipal());
        return ResponseEntity.ok(fileMapper.toDto(file));
    }

}
