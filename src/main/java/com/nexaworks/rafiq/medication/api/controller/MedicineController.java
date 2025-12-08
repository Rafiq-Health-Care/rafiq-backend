package com.nexaworks.rafiq.medication.api.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.medication.api.dto.request.*;
import com.nexaworks.rafiq.medication.api.dto.response.AddResponse;
import com.nexaworks.rafiq.medication.api.dto.response.BulkOperationResponse;
import com.nexaworks.rafiq.medication.api.dto.response.MedicineGroupResponse;
import com.nexaworks.rafiq.medication.api.dto.response.MedicineResponse;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.mapper.MedicineMapper;
import com.nexaworks.rafiq.medication.service.MedicineService;
import com.nexaworks.rafiq.shared.dto.PageResponse;
import com.nexaworks.rafiq.shared.mapper.PageMapper;

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
@Tag(name = "Medicine Management")
public class MedicineController {
    private final MedicineService medicineService;
    private final MedicineMapper medicineMapper;
    private final PageMapper pageMapper;

    private UUID getUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/add")
    @Operation(summary = "Add medicine", description = "Adds a new medicine to user's medication list. Enables tracking and reminder scheduling for prescribed medications.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> addMedicine(
            @Valid @RequestBody AddMedicineRequest request, Authentication authentication) {
        Medicine medicine = medicineService.addMedicine(medicineMapper.toEntity(request),
                request.medicineId(), getUserId(authentication));
        MedicineResponse medicineResponse = medicineMapper.toDto(medicine);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Medicine added successfully", medicineResponse));
    }

    @GetMapping
    @Operation(summary = "Get all medicines", description = "Retrieves paginated list of user's medicines with optional filtering. Supports grouping and search for efficient medication management.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<MedicineGroupResponse>> getAllMedicines(
            @ParameterObject Pageable pageable, @ParameterObject MedicineFilter filter,
            Authentication authentication) {
        return ResponseEntity.ok().body(pageMapper.map(
                medicineService.getAllMedicines(pageable, filter, getUserId(authentication)),
                medicineMapper::toGroupDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID", description = "Retrieves detailed information about a specific medicine. Used for viewing full medication details and editing.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MedicineResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MedicineResponse> getMedicine(@PathVariable("id") UUID medicineId,
            Authentication authentication) {
        Medicine medicine = medicineService.getMedicineById(medicineId, getUserId(authentication));
        MedicineResponse medicineResponse = medicineMapper.toDto(medicine);
        return ResponseEntity.ok().body(medicineResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine", description = "Removes a medicine from user's medication list. Use when medication is no longer needed or was added by mistake.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteMedicine(@PathVariable("id") UUID medicineId,
            Authentication authentication) {
        medicineService.deleteMedicine(medicineId, getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine", description = "Updates all medicine details including dosage, frequency, and schedule. Use when prescription changes or medication details need correction.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> updateMedicine(
            @PathVariable("id") UUID medicineId, @Valid @RequestBody UpdateMedicineRequest request,
            Authentication authentication) {
        Medicine medicine = medicineService.updateMedicine(medicineMapper.toEntity(request),
                medicineId, getUserId(authentication));
        return ResponseEntity.ok().body(new AddResponse<>(true, "Medicine updated successfully",
                medicineMapper.toDto(medicine)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update medicine", description = "Updates specific medicine fields without affecting others. Ideal for quick adjustments like changing reminder times or dosage without full form submission.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<MedicineResponse>> updateSpecific(
            @PathVariable("id") UUID medicineId, @RequestBody UpdateMedicinePatchRequest request,
            Authentication authentication) throws SchedulerException {
        Medicine medicine = medicineService.updateSpecific(medicineId, request,
                getUserId(authentication));
        return ResponseEntity.ok().body(new AddResponse<>(true, "Medicine updated successfully",
                medicineMapper.toDto(medicine)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk medicine operations", description = "Performs operations on multiple medicines at once. Enables efficient batch updates, deletions, or status changes for users managing multiple medications.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<BulkOperationResponse>> bulkAddMedicine(
            @Valid @RequestBody BulkMedicineOperationRequest request, Authentication authentication)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<UUID> failedIds = medicineService.bulkMedicineOperation(request,
                getUserId(authentication));
        BulkOperationResponse response = new BulkOperationResponse(
                request.medicineIds().size() - failedIds.size(), failedIds);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Bulk operation completed successfully", response));
    }
}
