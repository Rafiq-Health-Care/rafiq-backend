package com.nexaworks.rafiq.service.medicine;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.medicine.*;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicineGroupResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicineResponse;
import com.nexaworks.rafiq.entities.Medicine;

import jakarta.validation.Valid;

public interface IMedicineService {
    MedicineResponse addMedicine(AddMedicineRequest request);

    PageResponse<MedicineGroupResponse> getAllMedicines(Pageable pageable, MedicineFilter filter);

    MedicineResponse getMedicineById(UUID medicineId);

    void deleteMedicine(UUID medicineId);

    MedicineResponse updateMedicine(UpdateMedicineRequest request, UUID medicineId);

    MedicineResponse updateSpecific(UUID medicineId, UpdateMedicinePatchRequest request);

    Medicine getMedicineEntityById(UUID medicineId);

    List<UUID> bulkMedicineOperation(@Valid BulkMedicineOperationRequest request)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
    void moveToGroup(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds);

}
