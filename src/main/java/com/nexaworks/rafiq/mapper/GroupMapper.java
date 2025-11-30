package com.nexaworks.rafiq.mapper;

import java.util.Collections;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.Group.GroupDetailsResponse;
import com.nexaworks.rafiq.dto.response.Group.GroupResponse;
import com.nexaworks.rafiq.dto.response.common.Response;
import com.nexaworks.rafiq.entities.Group;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toEntity(AddGroupRequest addGroupRequest);

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "id", target = "groupId")
    @Mapping(target = "medicineCount", expression = "java(group.getMedicines() != null ? group.getMedicines().size() : 0)")
    AddGroupResponse toDto(Group savedGroup);

    default GroupResponse toGroupDto(Group group, MedicineMapper medicineMapper) {
        return new GroupResponse(group.getId(), group.getName(), group.getDescription(),
                group.getColor(), group.getIconUrl(),
                group.getMedicines() != null ? group.getMedicines().size() : 0,
                group.getMedicines() != null
                        ? group.getMedicines().stream().map(medicineMapper::toPreviewDto).toList()
                        : Collections.emptyList()

        );
    }
    default Response<GroupDetailsResponse> toResponse(Group group, MedicineMapper medicineMapper) {
        return new Response<>(true, new GroupDetailsResponse(group.getId(),
                group.getPatient().getId(), group.getName(), group.getDescription(),
                group.getColor(), group.getIconUrl(),
                group.getMedicines() != null ? group.getMedicines().size() : 0,
                group.getMedicines() != null
                        ? group.getMedicines().stream().map(medicineMapper::toGroupDto).toList()
                        : Collections.emptyList(),
                group.getCreatedAt(), group.getUpdatedAt()));
    }
}
