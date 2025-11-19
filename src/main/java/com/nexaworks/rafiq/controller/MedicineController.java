package com.nexaworks.rafiq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.dto.request.AddMedicineRequest;
import com.nexaworks.rafiq.dto.response.AddResponse;
import com.nexaworks.rafiq.dto.response.MedicineResponse;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.service.MedicineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicine")
@RequiredArgsConstructor
public class MedicineController {
    private final MedicineService medicineService;
    private final MedicineMapper medicineMapper;
    @PostMapping("/add")
    public ResponseEntity<AddResponse<MedicineResponse>> addMedicine(
            @Valid @RequestBody AddMedicineRequest request) {
        Medicine medicine = medicineService.addMedicine(medicineMapper.toEntity(request),
                request.medicineId());
        MedicineResponse medicineResponse = medicineMapper.toDto(medicine);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddResponse<MedicineResponse>(
                true, "Medicine added successfully", medicineResponse));
    }

}
