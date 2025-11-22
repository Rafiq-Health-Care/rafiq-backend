package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.exception.custom.GroupNotFoundException;
import com.nexaworks.rafiq.repository.GroupRepository;
import com.nexaworks.rafiq.service.GroupService;
import com.nexaworks.rafiq.service.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final PatientService patientService;

    @Override
    public Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId).orElseThrow(
                () -> new GroupNotFoundException("Group not found with id: " + groupId));
    }

    @Override
    @Transactional
    public Group addGroup(Group group) {
        group.setPatientProfile(patientService.getPatientProfile());
        return groupRepository.save(group);
    }

    @Override
    public Page<Group> getGroups(int page, int size, String direction, String sort) {
        Sort sortOrder = direction.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        UUID patientId = patientService.getPatientProfile().getId();
        return groupRepository.findByPatientProfileId(patientId, pageable);
    }

}
