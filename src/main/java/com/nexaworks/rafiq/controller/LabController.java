package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.request.AddLabRequest;
import com.nexaworks.rafiq.mapper.AddressMapper;
import com.nexaworks.rafiq.service.LabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/labs")
@RequiredArgsConstructor
public class LabController {
    private final LabService labService;
    private final AddressMapper addressMapper;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addLab(@RequestPart("lab") @Valid AddLabRequest request,
                                       @RequestPart("logo")MultipartFile file) throws IOException {
        labService.addLab(request.name(),addressMapper.toEntity(request.addresses()),file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
//    @GetMapping
//    public ResponseEntity<> getAllLabs(){


}
