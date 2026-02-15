package com.nexaworks.rafiq.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.request.group.AddMedicinesToGroup;
import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.Group.AddMedicineToGroupResponse;
import com.nexaworks.rafiq.dto.response.Group.GroupDetailsResponse;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.common.Response;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.mapper.GroupMapper;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.service.medicine.GroupService;
import com.nexaworks.rafiq.service.medicine.MedicineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Tag(name = "Medicine Group Management", description = "Endpoints for organizing medicines into groups")
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final PageMapper pageMapper;
    private final MedicineMapper medicineMapper;
    private final MedicineService medicineService;
    @PostMapping("/add")
    @Operation(summary = "Add medicine group", description = "Creates a new group to organize medicines by category or condition.")
    @ApiResponse(responseCode = "201", description = "Group added successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddGroupResponse>> addGroup(
            @Valid @RequestBody AddGroupRequest request) {
        Group group = groupMapper.toEntity(request);
        Group savedGroup = groupService.addGroup(group);
        AddGroupResponse response = groupMapper.toDto(savedGroup);
        return ResponseEntity.status(201)
                .body(new AddResponse<>(true, "Group added successfully", response));
    }
    @GetMapping
    @Operation(summary = "Get all groups", description = "Retrieves paginated list of medicine groups with sorting options.")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<AddGroupResponse>> getGroups(
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Page<Group> groups = groupService.getGroups(page, size, direction, sort);
        return ResponseEntity.ok().body(pageMapper.mapToGroupPage(groups, groupMapper));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID", description = "Retrieves detailed information about a specific group including all medicines.")
    @ApiResponse(responseCode = "200", description = "Group retrieved successfully", content = @Content(schema = @Schema(implementation = Response.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Response<GroupDetailsResponse>> getGroup(@PathVariable UUID id) {
        Group group = groupService.getGroupById(id);
        GroupDetailsResponse response = groupMapper.toResponse(group, medicineMapper);
        return ResponseEntity.ok().body(new Response<>(true, response));
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Update group", description = "Updates group name or description.")
    @ApiResponse(responseCode = "200", description = "Group updated successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddGroupResponse>> updateGroup(
            @Valid @RequestBody UpdateGroupRequest request, @PathVariable UUID id) {
        Group updatedGroup = groupService.updateGroupById(request, id);
        AddGroupResponse response = groupMapper.toDto(updatedGroup);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Group updated successfully", response));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete group", description = "Removes a group. Medicines are moved back to ungrouped status.")
    @ApiResponse(responseCode = "204", description = "Group deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroupById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addMedicines/{groupId}")
    @Operation(summary = "Add medicines to group", description = "Moves multiple medicines into a group.")
    @ApiResponse(responseCode = "200", description = "Medicines added to group successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Remove medicine from group", description = "Removes a medicine from a group, returning it to ungrouped status.")
    @ApiResponse(responseCode = "200", description = "Medicine removed from group successfully")
    @SecurityRequirement(name = "bearerAuth")
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
