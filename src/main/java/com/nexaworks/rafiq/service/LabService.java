package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Address;
import com.nexaworks.rafiq.entities.Lab;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface LabService {
  void addLab(@NotBlank String name, List<Address> entity, MultipartFile file) throws IOException;

  Page<Lab> getAll(int page, int size, String sort, String direction);

  void deleteLab(UUID labId);

  void updateLab(@NotBlank String name, List<Address> entity, MultipartFile file, UUID labId)
      throws IOException;

  Optional<Lab> getLabById(UUID s);

  Lab save(Lab lab);
}
