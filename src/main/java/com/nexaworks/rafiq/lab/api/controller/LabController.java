package com.nexaworks.rafiq.lab.api.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.lab.api.dto.AddLabRequest;
import com.nexaworks.rafiq.shared.dto.PageResponse;
import com.nexaworks.rafiq.lab.api.dto.LabResponse;
import com.nexaworks.rafiq.shared.mapper.PageMapper;
import com.nexaworks.rafiq.lab.service.LabService;
import com.nexaworks.rafiq.shared.mapper.AddressMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/labs")
@RequiredArgsConstructor
public class LabController {
    private final LabService labService;
    private final AddressMapper addressMapper;
    private final PageMapper pageMapper;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addLab(@RequestPart("lab") @Valid AddLabRequest request,
            @RequestPart("logo") MultipartFile file) throws IOException {
        labService.addLab(request.name(), addressMapper.toEntity(request.addresses()), file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<LabResponse>> getAllLabs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        return ResponseEntity.ok()
                .body(pageMapper.mapToLabPage(labService.getAll(page, size, sort, direction)));
    }

    @PutMapping("/{lab-id}")
    public ResponseEntity<Void> updateLab(@RequestPart("lab") @Valid AddLabRequest request,
            @RequestPart("logo") MultipartFile file, @PathVariable("lab-id") UUID labId)
            throws IOException {
        labService.updateLab(request.name(), addressMapper.toEntity(request.addresses()), file,
                labId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{lab-id}")
    public ResponseEntity<Void> updateLab(@PathVariable("lab-id") UUID labId) {
        labService.deleteLab(labId);
        return ResponseEntity.ok().build();
    }
}
