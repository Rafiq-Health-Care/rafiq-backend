package com.nexaworks.rafiq.service.medicine.implementation;

import java.util.Optional;
import java.util.UUID;

import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.service.authentication.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.exception.custom.GroupIsAlreadyExistsException;
import com.nexaworks.rafiq.exception.custom.GroupNotFoundException;
import com.nexaworks.rafiq.exception.custom.MedicineNotFound;
import com.nexaworks.rafiq.repository.GroupRepository;
import com.nexaworks.rafiq.service.medicine.GroupService;
import com.nexaworks.rafiq.service.patient.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final PatientService patientService;
    private final AuthService authService;

    @Override
    public Group getGroupById(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(
                () -> new GroupNotFoundException("Group not found with id: " + groupId));
        UUID patientId = patientService.getPatientProfile().getId();
        if (!group.getPatient().getId().equals(patientId)) {
            throw new GroupNotFoundException("Group not found with id: " + groupId);
        }
        return group;
    }

    @Override
    @Transactional
    public Group addGroup(Group group) {
        Patient patient = (Patient) authService.getAuthenticateUser();
        if (groupRepository.existsGroupByName_AndPatient(group.getName(), patient)) {
            throw new GroupIsAlreadyExistsException(
                    "Group with name " + group.getName() + " already exists");
        }
        group.setPatient(patient);
        return groupRepository.save(group);
    }

    @Override
    public Page<Group> getGroups(int page, int size, String direction, String sort) {
        Sort sortOrder = direction.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        UUID patientId = authService.getAuthenticateUserId();
        return groupRepository.findByPatientId(patientId, pageable);
    }

    @Override
    @Transactional
    public Group updateGroupById(UpdateGroupRequest request, UUID id) {
        Group existingGroup = getGroupById(id);
        Optional.ofNullable(request.name()).ifPresent(name -> {
            if (groupRepository.existsGroupByPatient_IdAndName(
                    patientService.getPatientProfile().getId(), name)) {
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
    public void deleteGroupById(UUID id) {
        Group group = getGroupById(id);
        groupRepository.delete(group);
    }

    @Override
    public void removeFromGroup(UUID groupId, UUID medicineId) {
        Group group = getGroupById(groupId);
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
