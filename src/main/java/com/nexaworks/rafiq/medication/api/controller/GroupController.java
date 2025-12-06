package com.nexaworks.rafiq.medication.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.medication.api.dto.request.AddGroupRequest;
import com.nexaworks.rafiq.medication.api.dto.request.AddMedicinesToGroup;
import com.nexaworks.rafiq.medication.api.dto.request.UpdateGroupRequest;
import com.nexaworks.rafiq.medication.api.dto.response.AddGroupResponse;
import com.nexaworks.rafiq.medication.api.dto.response.AddMedicineToGroupResponse;
import com.nexaworks.rafiq.medication.api.dto.response.AddResponse;
import com.nexaworks.rafiq.medication.api.dto.response.GroupDetailsResponse;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.mapper.GroupMapper;
import com.nexaworks.rafiq.medication.mapper.MedicineMapper;
import com.nexaworks.rafiq.medication.service.GroupService;
import com.nexaworks.rafiq.medication.service.MedicineService;
import com.nexaworks.rafiq.shared.dto.PageResponse;
import com.nexaworks.rafiq.shared.dto.Response;
import com.nexaworks.rafiq.shared.mapper.PageMapper;

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
@Tag(name = "Medicine Group Management")
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final PageMapper pageMapper;
    private final MedicineMapper medicineMapper;
    private final MedicineService medicineService;

    private UUID getUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/add")
    @Operation(summary = "Add medicine group", description = "Creates a new group to organize medicines by category, condition, or time. Enables better medication management for users with multiple prescriptions.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddGroupResponse>> addGroup(
            @Valid @RequestBody AddGroupRequest request, Authentication authentication) {
        Group group = groupMapper.toEntity(request);
        Group savedGroup = groupService.addGroup(group, getUserId(authentication));
        AddGroupResponse response = groupMapper.toDto(savedGroup);
        return ResponseEntity.status(201)
                .body(new AddResponse<>(true, "Group added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all groups", description = "Retrieves paginated list of user's medicine groups with sorting options. Helps users navigate and manage their organized medication collections.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<AddGroupResponse>> getGroups(
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Authentication authentication) {
        Page<Group> groups = groupService.getGroups(page, size, direction, sort,
                getUserId(authentication));
        return ResponseEntity.ok().body(pageMapper.mapToGroupPage(groups, groupMapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID", description = "Retrieves detailed information about a specific group including all medicines in it. Used for viewing group contents and managing group membership.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Response<GroupDetailsResponse>> getGroup(@PathVariable UUID id,
            Authentication authentication) {
        Group group = groupService.getGroupById(id, getUserId(authentication));
        GroupDetailsResponse response = groupMapper.toResponse(group, medicineMapper);
        return ResponseEntity.ok().body(new Response<>(true, response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update group", description = "Updates group name or description. Allows users to reorganize and rename groups as their medication needs change.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddGroupResponse>> updateGroup(
            @Valid @RequestBody UpdateGroupRequest request, @PathVariable UUID id,
            Authentication authentication) {
        Group updatedGroup = groupService.updateGroupById(request, id, getUserId(authentication));
        AddGroupResponse response = groupMapper.toDto(updatedGroup);
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Group updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete group", description = "Removes a group from user's collection. Medicines in the group are moved back to ungrouped status, not deleted.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id, Authentication authentication) {
        groupService.deleteGroupById(id, getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addMedicines/{groupId}")
    @Operation(summary = "Add medicines to group", description = "Moves multiple medicines into a group for better organization. Enables bulk categorization of medications by condition, time, or purpose.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddMedicineToGroupResponse>> addMedicinesToGroup(
            @PathVariable UUID groupId, @Valid @RequestBody AddMedicinesToGroup request,
            Authentication authentication) {
        List<UUID> movedMedicineIds = new ArrayList<>();
        medicineService.moveToGroup(request.medicineIds(), Optional.of(groupId), movedMedicineIds,
                getUserId(authentication));
        return ResponseEntity.status(200)
                .body(new AddResponse<>(true, "Medicines added to group successfully",
                        new AddMedicineToGroupResponse(groupId,
                                request.medicineIds().size() - movedMedicineIds.size())));
    }

    @PostMapping("/removeMedicines/{groupId}/{medicineId}")
    @Operation(summary = "Remove medicine from group", description = "Removes a medicine from a group, returning it to ungrouped status. Useful when medication no longer fits the group category.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> removeMedicineFromGroup(
            @PathVariable(name = "groupId") UUID groupId,
            @PathVariable(name = "medicineId") UUID medicineId, Authentication authentication) {
        groupService.removeFromGroup(groupId, medicineId, getUserId(authentication));
        return ResponseEntity.status(200).body("""
                {
                  "success": true,
                  "message": "Medicine removed from group"
                }""");
    }
}
