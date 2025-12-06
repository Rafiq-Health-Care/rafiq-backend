package com.nexaworks.rafiq.lab.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.lab.entity.Lab;
import com.nexaworks.rafiq.shared.entity.Address;

import jakarta.validation.constraints.NotBlank;

public interface LabService {
    void addLab(@NotBlank String name, List<Address> entity, MultipartFile file) throws IOException;

    Page<Lab> getAll(int page, int size, String sort, String direction);

    void deleteLab(UUID labId);

    void updateLab(@NotBlank String name, List<Address> entity, MultipartFile file, UUID labId)
            throws IOException;

    Optional<Lab> getLabById(UUID s);

    Lab save(Lab lab);
}
