package com.nexaworks.rafiq.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.medicine.*;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.dto.response.medicine.BulkOperationResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicineGroupResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicineResponse;
import com.nexaworks.rafiq.service.medicine.IMedicineService;

import dev.once.annotation.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicine")
@RequiredArgsConstructor
@Tag(name = "Medicine Management", description = "Endpoints for managing medicines, dosage, and bulk operations")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class MedicineController {
    private final IMedicineService IMedicineService;

    @PostMapping
    @Operation(summary = "Add medicine", description = "Adds a new medicine to user's medication list. Enables tracking and reminder scheduling.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Medicine added successfully"),
            @ApiResponse(responseCode = "409", description = "Medicine already exists"),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity (limit exceeded)")})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> addMedicine(
            @Valid @RequestBody AddMedicineRequest request) {
        MedicineResponse medicineResponse = IMedicineService.addMedicine(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Medicine added successfully", medicineResponse));
    }

    @GetMapping
    @Operation(summary = "Get all medicines", description = "Retrieves paginated list of medicines with optional filtering and grouping.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicines retrieved successfully")})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<MedicineGroupResponse>> getAllMedicines(
            @ParameterObject Pageable pageable, @ParameterObject MedicineFilter filter) {
        return ResponseEntity.ok().body(IMedicineService.getAllMedicines(pageable, filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID", description = "Retrieves detailed information about a specific medicine.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicine retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Medicine not found")})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MedicineResponse> getMedicine(@PathVariable("id") UUID medicineId) {
        MedicineResponse medicineResponse = IMedicineService.getMedicineById(medicineId);
        return ResponseEntity.ok().body(medicineResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine", description = "Removes a medicine from the medication list.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Medicine deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Medicine not found")})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteMedicine(@PathVariable("id") UUID medicineId) {
        IMedicineService.deleteMedicine(medicineId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine", description = "Updates all medicine details including dosage, frequency, and schedule.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicine updated successfully"),
            @ApiResponse(responseCode = "404", description = "Medicine not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity (limit or business rule)")})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> updateMedicine(
            @PathVariable("id") UUID medicineId,
            @Valid @RequestBody UpdateMedicineRequest request) {
        MedicineResponse medicineResponse = IMedicineService.updateMedicine(request, medicineId);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Medicine updated successfully", medicineResponse));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update medicine", description = "Updates specific medicine fields without affecting others.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicine updated successfully"),
            @ApiResponse(responseCode = "404", description = "Medicine not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity (business rule)")})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> updateSpecific(
            @PathVariable("id") UUID medicineId, @RequestBody UpdateMedicinePatchRequest request) {
        MedicineResponse medicineResponse = IMedicineService.updateSpecific(medicineId, request);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Medicine updated successfully", medicineResponse));
    }

    @Idempotent(force = true)
    @PostMapping("/bulk")
    @Operation(summary = "Bulk medicine operations", description = "Performs operations on multiple medicines at once.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk operation completed successfully"),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity (some items failed)")})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<BulkOperationResponse>> bulkAddMedicine(
            @Valid @RequestBody BulkMedicineOperationRequest request)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<UUID> failedIds = IMedicineService.bulkMedicineOperation(request);
        BulkOperationResponse response = new BulkOperationResponse(
                request.medicineIds().size() - failedIds.size(), failedIds);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Bulk operation completed successfully", response));
    }

}
