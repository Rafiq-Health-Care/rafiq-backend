package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.medicine.AddMedicineRequest;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicineRequest;
import com.nexaworks.rafiq.dto.response.medicine.MedicineResponse;
import com.nexaworks.rafiq.entities.Medicine;

@Mapper(componentModel = "spring")
public interface MedicineMapper {
    Medicine toEntity(AddMedicineRequest request);
    Medicine toEntity(UpdateMedicineRequest request);

    @Transactional
    default MedicineResponse toDto(Medicine entity) {
        if (entity == null) {
            return null;
        }

        return new MedicineResponse(entity.getId(),
                entity.getPatient() != null && entity.getPatient().getUser() != null
                        ? entity.getPatient().getUser().getId()
                        : null,
                entity.getDrug() != null ? entity.getDrug().getTradeName() : null,
                entity.getDosage(), entity.getFrequency(), entity.getStartDate(),
                entity.getEndDate(), entity.getNotes(), entity.getPhotoUrl(), entity.getType(),
                entity.getStatus(), entity.getGroup() == null ? null : entity.getGroup().getId(),
                entity.getGroup() == null ? null : entity.getGroup().getName(), 0, // reminderCount
                                                                                   // - can be
                                                                                   // updated later
                                                                                   // if
                // ReminderRepository is available
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

}
