package com.nexaworks.rafiq.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.nexaworks.rafiq.service.medicine.GroupService;
import com.nexaworks.rafiq.service.medicine.IMedicineService;

import dev.once.annotation.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
@Tag(name = "Medicine Group Management", description = "Endpoints for organizing medicines into groups")
public class GroupController {
    private final GroupService groupService;
    private final IMedicineService medicineService;

    @Idempotent(force = true)
    @PostMapping
    @Operation(summary = "Add medicine group", description = "Creates a new group to organize medicines by category or condition.")
    @ApiResponse(responseCode = "201", description = "Group added successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    public ResponseEntity<AddResponse<AddGroupResponse>> addGroup(
            @Valid @RequestBody AddGroupRequest request) {
        AddGroupResponse response = groupService.addGroup(request);
        return ResponseEntity.status(201)
                .body(new AddResponse<>(true, "Group added successfully", response));
    }
    @GetMapping
    @Operation(summary = "Get all groups", description = "Retrieves paginated list of medicine groups with sorting options.")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<PageResponse<AddGroupResponse>> getGroups(
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        return ResponseEntity.ok().body(groupService.getGroups(page, size, direction, sort));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID", description = "Retrieves detailed information about a specific group including all medicines.")
    @ApiResponse(responseCode = "200", description = "Group retrieved successfully", content = @Content(schema = @Schema(implementation = Response.class)))
    public ResponseEntity<Response<GroupDetailsResponse>> getGroup(@PathVariable UUID id) {
        GroupDetailsResponse response = groupService.getGroupResponse(id);
        return ResponseEntity.ok().body(new Response<>(true, response));
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Update group", description = "Updates group name or description.")
    @ApiResponse(responseCode = "200", description = "Group updated successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    public ResponseEntity<AddResponse<AddGroupResponse>> updateGroup(
            @Valid @RequestBody UpdateGroupRequest request, @PathVariable UUID id) {
        AddGroupResponse response = groupService.updateGroupById(request, id);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Group updated successfully", response));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete group", description = "Removes a group. Medicines are moved back to ungrouped status.")
    @ApiResponse(responseCode = "204", description = "Group deleted successfully")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroupById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addMedicines/{groupId}")
    @Operation(summary = "Add medicines to group", description = "Moves multiple medicines into a group.")
    @ApiResponse(responseCode = "200", description = "Medicines added to group successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    public ResponseEntity<AddResponse<AddMedicineToGroupResponse>> addMedicinesToGroup(
            @PathVariable UUID groupId, @Valid @RequestBody AddMedicinesToGroup request) {
        List<UUID> movedMedicineIds = new ArrayList<>();
        medicineService.moveToGroup(request.medicineIds(), Optional.of(groupId), movedMedicineIds);
        return ResponseEntity.status(200)
                .body(new AddResponse<>(true, "Medicines added to group successfully",
                        new AddMedicineToGroupResponse(groupId,
                                request.medicineIds().size() - movedMedicineIds.size())));
    }
    @PutMapping("/removeMedicines/{groupId}/{medicineId}")
    @Operation(summary = "Remove medicine from group", description = "Removes a medicine from a group, returning it to ungrouped status.")
    @ApiResponse(responseCode = "200", description = "Medicine removed from group successfully")
    public ResponseEntity<Void> removeMedicineFromGroup(@PathVariable UUID groupId,
            @PathVariable UUID medicineId) {
        groupService.removeFromGroup(groupId, medicineId);
        return ResponseEntity.status(200).build();
    }

}
