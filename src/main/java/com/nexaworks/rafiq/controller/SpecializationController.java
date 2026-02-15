package com.nexaworks.rafiq.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.response.specialization.SpecializationResponse;
import com.nexaworks.rafiq.service.doctor.SpecializationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/specialization")
@RequiredArgsConstructor
@Tag(name = "Specialization", description = "Endpoints for doctor specializations used in registration")
public class SpecializationController {
    private final SpecializationService specializationService;

    @GetMapping
    @Operation(summary = "Get all specializations", description = "Retrieves list of available doctor specializations for registration dropdown.")
    @ApiResponse(responseCode = "200", description = "Specializations retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SpecializationResponse.class))))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<SpecializationResponse>> getSpecializations() {
        return ResponseEntity.ok().body(specializationService.getSpecializations());
    }
}
