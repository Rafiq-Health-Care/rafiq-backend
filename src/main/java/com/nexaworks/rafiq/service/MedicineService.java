package com.nexaworks.rafiq.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.medicine.MedicineFilter;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicinePatchRequest;
import com.nexaworks.rafiq.entities.Medicine;

public interface MedicineService {
    Medicine addMedicine(Medicine entity, UUID medicineId);

    Page<Medicine> getAllMedicines(Pageable pageable, MedicineFilter filter);

    Medicine getMedicineById(UUID medicineId);

    void deleteMedicine(UUID medicineId);

    Medicine updateMedicine(Medicine entity, UUID medicineId);

    Medicine updateSpecific(UUID medicineId, UpdateMedicinePatchRequest request);
}
