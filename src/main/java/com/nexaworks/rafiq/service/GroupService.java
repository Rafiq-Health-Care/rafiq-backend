package com.nexaworks.rafiq.service;

import java.util.UUID;

import com.nexaworks.rafiq.entities.Group;

public interface GroupService {
    Group getGroupById(UUID groupId);
}
