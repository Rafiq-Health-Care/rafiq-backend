package com.nexaworks.rafiq.medication.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.medication.api.dto.request.UpdateGroupRequest;
import com.nexaworks.rafiq.medication.entity.model.Group;

import jakarta.validation.Valid;

public interface GroupService {
    Group getGroupById(UUID groupId, UUID patientId);

    Group addGroup(Group group, UUID patientId);

    Page<Group> getGroups(int page, int size, String direction, String sort, UUID patientId);

    Group updateGroupById(@Valid UpdateGroupRequest request, UUID id, UUID patientId);

    void deleteGroupById(UUID id, UUID patientId);

    void removeFromGroup(UUID groupId, UUID medicineId, UUID patientId);
}
