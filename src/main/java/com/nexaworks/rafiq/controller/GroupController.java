package com.nexaworks.rafiq.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.mapper.GroupMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.service.GroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final PageMapper pageMapper;
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

}
