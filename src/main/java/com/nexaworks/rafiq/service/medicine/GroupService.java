package com.nexaworks.rafiq.service.medicine;

import java.util.UUID;

import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.entities.Group;

import jakarta.validation.Valid;

public interface GroupService {
    Group getGroupById(UUID groupId);

    com.nexaworks.rafiq.dto.response.Group.AddGroupResponse addGroup(
            com.nexaworks.rafiq.dto.request.group.AddGroupRequest request);

    PageResponse<AddGroupResponse> getGroups(int page, int size, String direction, String sort);

    com.nexaworks.rafiq.dto.response.Group.GroupDetailsResponse getGroupResponse(UUID id);

    AddGroupResponse updateGroupById(@Valid UpdateGroupRequest request, UUID id);

    void deleteGroupById(UUID id);

    void removeFromGroup(UUID groupId, UUID medicineId);
}
