package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.dto.request.medicine.AddMedicineRequest;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicineRequest;
import com.nexaworks.rafiq.dto.response.medicine.MedicineGroupResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicinePreview;
import com.nexaworks.rafiq.dto.response.medicine.MedicineResponse;
import com.nexaworks.rafiq.entities.Medicine;

@Mapper(componentModel = "spring")
public interface MedicineMapper {
    Medicine toEntity(AddMedicineRequest request);
    Medicine toEntity(UpdateMedicineRequest request);

    default MedicineResponse toDto(Medicine entity) {
        if (entity == null) {
            return null;
        }

        return new MedicineResponse(entity.getId(),
                entity.getPatient() != null ? entity.getPatient().getId() : null, entity.getName(),
                entity.getDosage(), entity.getFrequency(), entity.getReminderFrequency(),
                entity.getCustomDays(), entity.getStartDate(), entity.getEndDate(),
                entity.getNotes(), entity.getPhotoUrl(), entity.getType(), entity.getStatus(),
                entity.getGroup() != null ? entity.getGroup().getId() : null,
                entity.getGroup() != null ? entity.getGroup().getName() : null,
                entity.getReminder() == null ? null : entity.getReminder().getId(),
                entity.getReminder() == null ? null : entity.getReminder().getNextReminder(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    MedicinePreview toPreviewDto(Medicine entity);
    default MedicineGroupResponse toGroupDto(Medicine entity) {
        if (entity == null) {
            return null;
        }
        return new MedicineGroupResponse(entity.getId(), entity.getName(), entity.getDosage(),
                entity.getFrequency(), entity.getStatus(),
                entity.getReminder() == null ? null : entity.getReminder().getNextReminder());
    }

}
