package com.nexaworks.rafiq.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.lab.AddLabRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.lab.LabResponse;
import com.nexaworks.rafiq.mapper.AddressMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.service.lab.LabService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/labs")
@RequiredArgsConstructor
@Tag(name = "Lab Management", description = "Endpoints for managing lab facilities and their information")
public class LabController {
    private final LabService labService;
    private final AddressMapper addressMapper;
    private final PageMapper pageMapper;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add lab", description = "Creates a new lab with address and logo. Admin/doctor functionality.")
    @ApiResponse(responseCode = "201", description = "Lab added successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> addLab(@RequestPart("lab") @Valid AddLabRequest request,
            @RequestPart("logo") MultipartFile file) throws IOException {
        labService.addLab(request.name(), addressMapper.toEntity(request.addresses()), file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "Get all labs", description = "Retrieves paginated list of labs with sorting.")
    @ApiResponse(responseCode = "200", description = "Labs retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<LabResponse>> getAllLabs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        return ResponseEntity.ok()
                .body(pageMapper.mapToLabPage(labService.getAll(page, size, sort, direction)));
    }

    @PutMapping("/{lab-id}")
    @Operation(summary = "Update lab", description = "Updates lab information including name, address, and logo.")
    @ApiResponse(responseCode = "200", description = "Lab updated successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> updateLab(@RequestPart("lab") @Valid AddLabRequest request,
            @RequestPart("logo") MultipartFile file, @PathVariable("lab-id") UUID labId)
            throws IOException {
        labService.updateLab(request.name(), addressMapper.toEntity(request.addresses()), file,
                labId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{lab-id}")
    @Operation(summary = "Delete lab", description = "Removes a lab from the system.")
    @ApiResponse(responseCode = "200", description = "Lab deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> updateLab(@PathVariable("lab-id") UUID labId) {
        labService.deleteLab(labId);
        return ResponseEntity.ok().build();
    }
}
