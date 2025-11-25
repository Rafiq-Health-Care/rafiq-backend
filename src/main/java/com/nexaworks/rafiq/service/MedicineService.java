package com.nexaworks.rafiq.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.medicine.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.dto.request.medicine.MedicineFilter;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicinePatchRequest;
import com.nexaworks.rafiq.entities.Medicine;

import jakarta.validation.Valid;

public interface MedicineService {
    Medicine addMedicine(Medicine entity, UUID medicineId);

    Page<Medicine> getAllMedicines(Pageable pageable, MedicineFilter filter);

    Medicine getMedicineById(UUID medicineId);

    void deleteMedicine(UUID medicineId) throws SchedulerException;

    Medicine updateMedicine(Medicine entity, UUID medicineId);

    Medicine updateSpecific(UUID medicineId, UpdateMedicinePatchRequest request);

    List<UUID> bulkMedicineOperation(@Valid BulkMedicineOperationRequest request)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
    void moveToGroup(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds);

}
