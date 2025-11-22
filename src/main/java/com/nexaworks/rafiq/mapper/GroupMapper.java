package com.nexaworks.rafiq.mapper;

import java.util.Collections;

import org.mapstruct.Mapper;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.Group.GroupResponse;
import com.nexaworks.rafiq.entities.Group;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toEntity(AddGroupRequest addGroupRequest);

    default AddGroupResponse toDto(Group savedGroup) {
        return new AddGroupResponse(savedGroup.getId(), savedGroup.getPatientProfile().getId(),
                savedGroup.getDescription(), savedGroup.getColor(), savedGroup.getName(),
                savedGroup.getCreatedAt().toEpochMilli(), savedGroup.getUpdatedAt().toEpochMilli(),
                savedGroup.getIconUrl(),
                savedGroup.getMedicines() != null ? savedGroup.getMedicines().size() : 0);
    }
    @Transactional
    default GroupResponse toGroupDto(Group group, MedicineMapper medicineMapper) {
        return new GroupResponse(group.getId(), group.getName(), group.getDescription(),
                group.getColor(), group.getIconUrl(),
                group.getMedicines() != null ? group.getMedicines().size() : 0,
                group.getMedicines() != null
                        ? group.getMedicines().stream().map(medicineMapper::toDto).toList()
                        : Collections.emptyList()

        );
    }
}
