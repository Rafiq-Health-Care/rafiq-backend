package com.nexaworks.rafiq.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.Response;
import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.request.group.AddMedicinesToGroup;
import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.Group.AddMedicineToGroupResponse;
import com.nexaworks.rafiq.dto.response.Group.GroupDetailsResponse;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.mapper.GroupMapper;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.service.GroupService;
import com.nexaworks.rafiq.service.MedicineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final PageMapper pageMapper;
    private final MedicineMapper medicineMapper;
    private final MedicineService medicineService;
    @PostMapping("/add")
    public ResponseEntity<AddResponse<AddGroupResponse>> addGroup(
            @Valid @RequestBody AddGroupRequest request) {
        Group group = groupMapper.toEntity(request);
        Group savedGroup = groupService.addGroup(group);
        AddGroupResponse response = groupMapper.toDto(savedGroup);
        return ResponseEntity.status(201)
                .body(new AddResponse<>(true, "Group added successfully", response));
    }
    @GetMapping
    public ResponseEntity<PageResponse<AddGroupResponse>> getGroups(
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Page<Group> groups = groupService.getGroups(page, size, direction, sort);
        return ResponseEntity.ok().body(pageMapper.mapToGroupPage(groups));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response<GroupDetailsResponse>> getGroup(@PathVariable UUID id) {
        Group group = groupService.getGroupById(id);
        Response<GroupDetailsResponse> response = groupMapper.toResponse(group, medicineMapper);
        return ResponseEntity.ok().body(response);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<AddResponse<AddGroupResponse>> updateGroup(
            @Valid @RequestBody UpdateGroupRequest request, @PathVariable UUID id) {
        Group updatedGroup = groupService.updateGroupById(request, id);
        AddGroupResponse response = groupMapper.toDto(updatedGroup);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Group updated successfully", response));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroupById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addMedicines/{groupId}")
    public ResponseEntity<AddResponse<AddMedicineToGroupResponse>> addMedicinesToGroup(
            @PathVariable UUID groupId, @Valid @RequestBody AddMedicinesToGroup request) {
        List<UUID> movedMedicineIds = new ArrayList<>();
        medicineService.moveToGroup(request.medicineIds(), Optional.of(groupId), movedMedicineIds);
        return ResponseEntity.status(200)
                .body(new AddResponse<>(true, "Medicines added to group successfully",
                        new AddMedicineToGroupResponse(groupId,
                                request.medicineIds().size() - movedMedicineIds.size())));
    }
    @PostMapping("/removeMedicines/{groupId}/{medicineId}")
    public ResponseEntity<String> removeMedicineFromGroup(
            @PathVariable(name = "groupId") UUID groupId,
            @PathVariable(name = "medicineId") UUID medicineId) {
        groupService.removeFromGroup(groupId, medicineId);
        return ResponseEntity.status(200).body("""
                {
                  "success": true,
                  "message": "Medicine removed from group"
                }""");
    }

}
