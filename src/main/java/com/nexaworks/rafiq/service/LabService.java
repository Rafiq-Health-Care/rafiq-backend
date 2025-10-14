package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Address;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LabService {
    void addLab(@NotBlank String name, List<Address> entity, MultipartFile file) throws IOException;
}
