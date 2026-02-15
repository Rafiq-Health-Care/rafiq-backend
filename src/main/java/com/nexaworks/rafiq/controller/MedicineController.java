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
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.mapper.ReminderMapper;
import com.nexaworks.rafiq.service.medicine.MedicineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Management", description = "Endpoints for managing medicines, dosage, and bulk operations")
public class MedicineController {
    private final MedicineService medicineService;
    private final MedicineMapper medicineMapper;
    private final PageMapper pageMapper;
    private final ReminderMapper reminderMapper;
    @PostMapping("/add")
    @Operation(summary = "Add medicine", description = "Adds a new medicine to user's medication list. Enables tracking and reminder scheduling.")
    @ApiResponse(responseCode = "201", description = "Medicine added successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> addMedicine(
            @Valid @RequestBody AddMedicineRequest request) {
        Medicine medicine = medicineService.addMedicine(medicineMapper.toEntity(request),
                request.medicineId());
        MedicineResponse medicineResponse = medicineMapper.toDto(medicine);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Medicine added successfully", medicineResponse));
    }
    @GetMapping
    @Operation(summary = "Get all medicines", description = "Retrieves paginated list of medicines with optional filtering and grouping.")
    @ApiResponse(responseCode = "200", description = "Medicines retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<MedicineGroupResponse>> getAllMedicines(
            @ParameterObject Pageable pageable, @ParameterObject MedicineFilter filter) {
        return ResponseEntity.ok().body(pageMapper.mapToMedicinePage(
                medicineService.getAllMedicines(pageable, filter), medicineMapper));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID", description = "Retrieves detailed information about a specific medicine.")
    @ApiResponse(responseCode = "200", description = "Medicine retrieved successfully", content = @Content(schema = @Schema(implementation = MedicineResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MedicineResponse> getMedicine(@PathVariable("id") UUID medicineId) {
        Medicine medicine = medicineService.getMedicineById(medicineId);
        MedicineResponse medicineResponse = medicineMapper.toDto(medicine);
        return ResponseEntity.ok().body(medicineResponse);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine", description = "Removes a medicine from the medication list.")
    @ApiResponse(responseCode = "204", description = "Medicine deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteMedicine(@PathVariable("id") UUID medicineId) {
        medicineService.deleteMedicine(medicineId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update medicine", description = "Updates all medicine details including dosage, frequency, and schedule.")
    @ApiResponse(responseCode = "200", description = "Medicine updated successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> updateMedicine(
            @PathVariable("id") UUID medicineId,
            @Valid @RequestBody UpdateMedicineRequest request) {
        Medicine medicine = medicineService.updateMedicine(medicineMapper.toEntity(request),
                medicineId);
        return ResponseEntity.ok().body(new AddResponse<>(true, "Medicine updated successfully",
                medicineMapper.toDto(medicine)));
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Partially update medicine", description = "Updates specific medicine fields without affecting others.")
    @ApiResponse(responseCode = "200", description = "Medicine updated successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> updateSpecific(
            @PathVariable("id") UUID medicineId, @RequestBody UpdateMedicinePatchRequest request) {
        Medicine medicine = medicineService.updateSpecific(medicineId, request);
        return ResponseEntity.ok().body(new AddResponse<>(true, "Medicine updated successfully",
                medicineMapper.toDto(medicine)));
    }
    @PostMapping("/bulk")
    @Operation(summary = "Bulk medicine operations", description = "Performs operations on multiple medicines at once.")
    @ApiResponse(responseCode = "200", description = "Bulk operation completed successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<BulkOperationResponse>> bulkAddMedicine(
            @Valid @RequestBody BulkMedicineOperationRequest request)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<UUID> failedIds = medicineService.bulkMedicineOperation(request);
        BulkOperationResponse response = new BulkOperationResponse(
                request.medicineIds().size() - failedIds.size(), failedIds);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Bulk operation completed successfully", response));
    }

}
