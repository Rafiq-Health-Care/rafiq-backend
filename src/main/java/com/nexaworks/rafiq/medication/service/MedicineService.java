package com.nexaworks.rafiq.medication.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.medication.api.dto.request.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.medication.api.dto.request.MedicineFilter;
import com.nexaworks.rafiq.medication.api.dto.request.UpdateMedicinePatchRequest;
import com.nexaworks.rafiq.medication.entity.model.Medicine;

import jakarta.validation.Valid;

public interface MedicineService {
    Medicine addMedicine(Medicine entity, UUID medicineId, UUID patientId);

    Page<Medicine> getAllMedicines(Pageable pageable, MedicineFilter filter, UUID patientId);

    Medicine getMedicineById(UUID medicineId, UUID patientId);

    void deleteMedicine(UUID medicineId, UUID patientId);

    Medicine updateMedicine(Medicine entity, UUID medicineId, UUID patientId);

    Medicine updateSpecific(UUID medicineId, UpdateMedicinePatchRequest request, UUID patientId);

    List<UUID> bulkMedicineOperation(@Valid BulkMedicineOperationRequest request, UUID patientId)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
    void moveToGroup(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds, UUID patientId);

}
