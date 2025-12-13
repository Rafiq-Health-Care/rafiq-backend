package com.nexaworks.rafiq.medication.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.medication.api.dto.request.AddGroupRequest;
import com.nexaworks.rafiq.medication.api.dto.response.AddGroupResponse;
import com.nexaworks.rafiq.medication.api.dto.response.GroupDetailsResponse;
import com.nexaworks.rafiq.medication.api.dto.response.GroupResponse;
import com.nexaworks.rafiq.medication.entity.model.Group;

@Mapper(componentModel = "spring", uses = MedicineMapper.class)
public interface GroupMapper {
    Group toEntity(AddGroupRequest addGroupRequest);

    @Mapping(target = "groupId", source = "id")
    @Mapping(target = "medicineCount", expression = "java(group.getMedicines() != null ? group.getMedicines().size() : 0)")
    AddGroupResponse toDto(Group group);

    @Mapping(target = "medicineCount", expression = "java(group.getMedicines() != null ? group.getMedicines().size() : 0)")
    @Mapping(target = "medicinePreviews", expression = "java(group.getMedicines() != null ? "
            + "group.getMedicines().stream().map(medicineMapper::toPreviewDto).toList() : "
            + "java.util.Collections.emptyList())")
    GroupResponse toGroupDto(Group group, MedicineMapper medicineMapper);

    @Mapping(target = "medicineCount", expression = "java(group.getMedicines() != null ? group.getMedicines().size() : 0)")
    @Mapping(target = "medicines", expression = "java(group.getMedicines() != null ? "
            + "group.getMedicines().stream().map(medicineMapper::toGroupDto).toList() : "
            + "java.util.Collections.emptyList())")
    GroupDetailsResponse toResponse(Group group, MedicineMapper medicineMapper);
}
