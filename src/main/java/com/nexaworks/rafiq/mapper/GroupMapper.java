package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.Group.GroupDetailsResponse;
import com.nexaworks.rafiq.dto.response.Group.GroupResponse;
import com.nexaworks.rafiq.entities.Group;

@Mapper(componentModel = "spring", uses = MedicineMapper.class)
public interface GroupMapper {
    Group toEntity(AddGroupRequest addGroupRequest);

    @Mapping(target = "groupId", source = "id")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "medicineCount", expression = "java(group.getMedicines() != null ? group.getMedicines().size() : 0)")
    AddGroupResponse toDto(Group group);

    @Mapping(target = "medicineCount", expression = "java(group.getMedicines() != null ? group.getMedicines().size() : 0)")
    @Mapping(target = "medicinePreviews", expression = "java(group.getMedicines() != null ? "
            + "group.getMedicines().stream().map(medicineMapper::toPreviewDto).toList() : "
            + "java.util.Collections.emptyList())")
    GroupResponse toGroupDto(Group group, MedicineMapper medicineMapper);

    @Mapping(target = "userId", expression = "java(group.getPatient().getId())")
    @Mapping(target = "medicineCount", expression = "java(group.getMedicines() != null ? group.getMedicines().size() : 0)")
    @Mapping(target = "medicines", expression = "java(group.getMedicines() != null ? "
            + "group.getMedicines().stream().map(medicineMapper::toGroupDto).toList() : "
            + "java.util.Collections.emptyList())")
    GroupDetailsResponse toResponse(Group group, MedicineMapper medicineMapper);
}
