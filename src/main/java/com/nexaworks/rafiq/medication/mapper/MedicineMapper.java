package com.nexaworks.rafiq.medication.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.medication.api.dto.request.AddMedicineRequest;
import com.nexaworks.rafiq.medication.api.dto.request.UpdateMedicineRequest;
import com.nexaworks.rafiq.medication.api.dto.response.MedicineGroupResponse;
import com.nexaworks.rafiq.medication.api.dto.response.MedicinePreview;
import com.nexaworks.rafiq.medication.api.dto.response.MedicineResponse;
import com.nexaworks.rafiq.medication.entity.model.Medicine;

@Mapper(componentModel = "spring")
public interface MedicineMapper {
    Medicine toEntity(AddMedicineRequest request);
    Medicine toEntity(UpdateMedicineRequest request);

    @Mapping(target = "groupId", expression = "java(entity.getGroup() != null ? entity.getGroup().getId() : null)")
    @Mapping(target = "reminderId", expression = "java(entity.getReminder() != null ? entity.getReminder().getId() : null)")
    @Mapping(target = "nextReminder", expression = "java(entity.getReminder() != null ? entity.getReminder().getNextReminder() : null)")
    @Mapping(target = "groupName", expression = "java(entity.getGroup() != null ? entity.getGroup().getName() : null)")
    MedicineResponse toDto(Medicine entity);

    MedicinePreview toPreviewDto(Medicine entity);
    @Mapping(target = "nextReminder", expression = "java(entity.getReminder() != null ? entity.getReminder().getNextReminder() : null)")
    @Mapping(target = "groupId", expression = "java(entity.getGroup() != null ? entity.getGroup().getId() : null)")
    @Mapping(target = "groupName", expression = "java(entity.getGroup() != null ? entity.getGroup().getName() : null)")
    @Mapping(target = "groupColor", expression = "java(entity.getGroup() != null ? entity.getGroup().getColor() : null)")
    MedicineGroupResponse toGroupDto(Medicine entity);

}
