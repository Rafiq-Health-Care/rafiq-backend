package com.nexaworks.rafiq.service.lab;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.lab.AddLabRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.lab.LabResponse;
import com.nexaworks.rafiq.entities.Lab;

@Deprecated
public interface LabService {
    void addLab(AddLabRequest request, MultipartFile file) throws IOException;

    PageResponse<LabResponse> getAll(int page, int size, String sort, String direction);

    void deleteLab(UUID labId);

    void updateLab(AddLabRequest request, MultipartFile file, UUID labId) throws IOException;

    Optional<Lab> getLabById(UUID s);

    Lab save(Lab lab);
}
