package com.nexaworks.rafiq.medication.service.implementation;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.medication.api.dto.request.UpdateGroupRequest;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.exception.GroupIsAlreadyExistsException;
import com.nexaworks.rafiq.medication.exception.GroupNotFoundException;
import com.nexaworks.rafiq.medication.exception.MedicineNotFound;
import com.nexaworks.rafiq.medication.repository.GroupRepository;
import com.nexaworks.rafiq.medication.service.GroupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;

    @Override
    public Group getGroupById(UUID groupId, UUID patientId) {
        Group group = groupRepository.findById(groupId).orElseThrow(
                () -> new GroupNotFoundException("Group not found with id: " + groupId));
        if (!group.getPatientId().equals(patientId)) {
            throw new GroupNotFoundException("Group not found with id: " + groupId);
        }
        return group;
    }

    @Override
    @Transactional
    public Group addGroup(Group group, UUID patientId) {

        if (groupRepository.existsGroupByPatientIdAndName(patientId, group.getName())) {
            throw new GroupIsAlreadyExistsException(
                    "Group with name " + group.getName() + " already exists");
        }
        group.setPatientId(patientId);
        return groupRepository.save(group);
    }

    @Override
    public Page<Group> getGroups(int page, int size, String direction, String sort,
            UUID patientId) {
        Sort sortOrder = direction.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return groupRepository.findByPatientId(patientId, pageable);
    }

    @Override
    @Transactional
    public Group updateGroupById(UpdateGroupRequest request, UUID id, UUID patientId) {
        Group existingGroup = getGroupById(id, patientId);
        Optional.ofNullable(request.name()).ifPresent(name -> {
            if (groupRepository.existsGroupByPatientIdAndName(patientId, name)) {
                throw new GroupIsAlreadyExistsException(
                        "Group with name " + name + " already exists");
            }
            existingGroup.setName(name);
        });
        Optional.ofNullable(request.description()).ifPresent(existingGroup::setDescription);
        Optional.ofNullable(request.color()).ifPresent(existingGroup::setColor);
        return groupRepository.save(existingGroup);
    }

    @Override
    public void deleteGroupById(UUID id, UUID patientId) {
        Group group = getGroupById(id, patientId);
        groupRepository.delete(group);
    }

    @Override
    public void removeFromGroup(UUID groupId, UUID medicineId, UUID patientId) {
        Group group = getGroupById(groupId, patientId);
        log.info("Removing medicine with id {} from group {}", medicineId, group.getName());
        Medicine medicine = group.getMedicines().stream()
                .filter(med -> med.getId().equals(medicineId)).findFirst()
                .orElseThrow(() -> new MedicineNotFound("Medicine with id " + medicineId
                        + " not found in group " + group.getName()));
        group.getMedicines().remove(medicine);
        medicine.setGroup(null);
        groupRepository.save(group);
    }

}
