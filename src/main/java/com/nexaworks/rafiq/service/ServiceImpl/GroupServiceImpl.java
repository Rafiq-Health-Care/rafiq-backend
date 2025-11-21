package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.exception.custom.GroupNotFoundException;
import com.nexaworks.rafiq.repository.GroupRepository;
import com.nexaworks.rafiq.service.GroupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    @Override
    public Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId).orElseThrow(
                () -> new GroupNotFoundException("Group not found with id: " + groupId));
    }
}
