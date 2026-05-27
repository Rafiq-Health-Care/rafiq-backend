package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.doctor.AddNewExperience;
import com.nexaworks.rafiq.dto.request.doctor.EditBiographyRequest;
import com.nexaworks.rafiq.dto.request.doctor.EditExperience;
import com.nexaworks.rafiq.dto.request.doctor.UpdateBasicInfoRequest;
import com.nexaworks.rafiq.exception.model.ErrorResponse;
import com.nexaworks.rafiq.service.doctor.IDoctorProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
@Tag(name = "Doctor Profile", description = "Endpoints for managing doctor profile information")
@ApiResponses({@ApiResponse(responseCode = "200", description = "Operation completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body or validation failure", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized – missing or invalid token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden – insufficient permissions", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
public class DoctorProfileController {

    private final IDoctorProfileService doctorProfileService;

    @Operation(summary = "Update doctor biography", description = "Partially updates the biography section of the authenticated doctor's profile")
    @PatchMapping("/biography")
    public ResponseEntity<Void> updateBiography(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Biography content to update", required = true, content = @Content(schema = @Schema(implementation = EditBiographyRequest.class))) @Valid @RequestBody EditBiographyRequest request) {
        doctorProfileService.updateBiography(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update doctor basic info", description = "Replaces all basic profile information (name, contact, specialty, etc.) for the authenticated doctor")
    @PutMapping("/basicInfo")
    public ResponseEntity<Void> updateBasicInfo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Complete basic info payload", required = true, content = @Content(schema = @Schema(implementation = UpdateBasicInfoRequest.class))) @Valid @RequestBody UpdateBasicInfoRequest request) {
        doctorProfileService.updateBasicInfo(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add a new experience entry", description = "Creates a new work experience record linked to the authenticated doctor's profile")
    @PostMapping("/experience")
    public ResponseEntity<Void> addNewExperience(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Experience details to add", required = true, content = @Content(schema = @Schema(implementation = AddNewExperience.class))) @Valid @RequestBody AddNewExperience request) {
        doctorProfileService.addNewExperience(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Edit an existing experience entry", description = "Replaces an existing experience record identified by its UUID")
    @PutMapping("/experience/{expId}")
    public ResponseEntity<Void> editExperience(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated experience details", required = true, content = @Content(schema = @Schema(implementation = EditExperience.class))) @Valid @RequestBody EditExperience request,
            @Parameter(description = "UUID of the experience entry to update", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID expId) {
        doctorProfileService.editExperience(expId, request);
        return ResponseEntity.ok().build();
    }
}
