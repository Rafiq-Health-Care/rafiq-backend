package com.nexaworks.rafiq.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.entities.Group;

import jakarta.validation.Valid;

public interface GroupService {
    Group getGroupById(UUID groupId);

    Group addGroup(Group group);

    Page<Group> getGroups(int page, int size, String direction, String sort);

    Group updateGroupById(@Valid UpdateGroupRequest request, UUID id);
}
