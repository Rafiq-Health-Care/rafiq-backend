package com.nexaworks.rafiq.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "groupId", expression = "java(entity.getGroup() != null ? entity.getGroup().getId() : null)")
    @Mapping(target = "reminderId", expression = "java(entity.getReminder() != null ? entity.getReminder().getId() : null)")
    @Mapping(target = "nextReminder", expression = "java(entity.getReminder() != null ? entity.getReminder().getNextReminder() : null)")
    @Mapping(target = "groupName", expression = "java(entity.getGroup() != null ? entity.getGroup().getName() : null)")
    MedicineResponse toDto(Medicine entity);

    MedicinePreview toPreviewDto(Medicine entity);
    @Mapping(target = "nextReminder", expression = "java(entity.getReminder() != null ? entity.getReminder().getNextReminder() : null)")
    MedicineGroupResponse toGroupDto(Medicine entity);

    default LocalDate map(Instant value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    default Instant map(LocalDate value) {
        return value == null ? null : value.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

}
