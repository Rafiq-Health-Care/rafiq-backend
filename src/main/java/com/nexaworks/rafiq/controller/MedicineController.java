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
import com.nexaworks.rafiq.dto.response.medicine.GetMedicineResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicineResponse;
import com.nexaworks.rafiq.dto.response.reminder.ReminderResponse;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.mapper.ReminderMapper;
import com.nexaworks.rafiq.service.MedicineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicineController {
    private final MedicineService medicineService;
    private final MedicineMapper medicineMapper;
    private final PageMapper pageMapper;
    private final ReminderMapper reminderMapper;
    @PostMapping("/add")
    public ResponseEntity<AddResponse<MedicineResponse>> addMedicine(
            @Valid @RequestBody AddMedicineRequest request) {
        Medicine medicine = medicineService.addMedicine(medicineMapper.toEntity(request),
                request.medicineId());
        MedicineResponse medicineResponse = medicineMapper.toDto(medicine);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Medicine added successfully", medicineResponse));
    }
    @GetMapping
    public ResponseEntity<PageResponse<MedicineResponse>> getAllMedicines(
            @ParameterObject Pageable pageable, @ParameterObject MedicineFilter filter) {
        return ResponseEntity.ok().body(
                pageMapper.mapToMedicinePage(medicineService.getAllMedicines(pageable, filter)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<GetMedicineResponse> getMedicine(@PathVariable("id") UUID medicineId) {
        Medicine medicine = medicineService.getMedicineById(medicineId);
        MedicineResponse medicineResponse = medicineMapper.toDto(medicine);
        List<ReminderResponse> reminders = medicine.getReminders().stream()
                .map(reminderMapper::toResponse).toList();
        return ResponseEntity.ok().body(new GetMedicineResponse(medicineResponse, reminders));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable("id") UUID medicineId) {
        medicineService.deleteMedicine(medicineId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<AddResponse<MedicineResponse>> updateMedicine(
            @PathVariable("id") UUID medicineId,
            @Valid @RequestBody UpdateMedicineRequest request) {
        Medicine medicine = medicineService.updateMedicine(medicineMapper.toEntity(request),
                medicineId);
        return ResponseEntity.ok().body(new AddResponse<>(true, "Medicine updated successfully",
                medicineMapper.toDto(medicine)));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<AddResponse<MedicineResponse>> updateSpecific(
            @PathVariable("id") UUID medicineId, @RequestBody UpdateMedicinePatchRequest request) {
        Medicine medicine = medicineService.updateSpecific(medicineId, request);
        return ResponseEntity.ok().body(new AddResponse<>(true, "Medicine updated successfully",
                medicineMapper.toDto(medicine)));
    }
    @PostMapping("/bulk")
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
