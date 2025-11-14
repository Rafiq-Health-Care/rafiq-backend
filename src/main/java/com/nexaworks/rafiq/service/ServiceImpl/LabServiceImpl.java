package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.UploadResults;
import com.nexaworks.rafiq.entities.Address;
import com.nexaworks.rafiq.entities.Lab;
import com.nexaworks.rafiq.entities.LabTest;
import com.nexaworks.rafiq.enums.UploadType;
import com.nexaworks.rafiq.exception.custom.LabException;
import com.nexaworks.rafiq.repository.LabRepository;
import com.nexaworks.rafiq.service.AddressService;
import com.nexaworks.rafiq.service.ImageService;
import com.nexaworks.rafiq.service.LabService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabServiceImpl implements LabService {
  private final LabRepository labRepository;
  private final AddressService addressService;
  private final ImageService imageService;

  @Override
  @Transactional
  public void addLab(String name, List<Address> entity, MultipartFile file) throws IOException {
    Lab lab = new Lab();
    lab.setName(name);
    setLogo(file, lab);
    labRepository.save(lab);
    entity.forEach(e -> e.setLab(lab));
    addressService.saveAll(entity);
  }

  @Override
  public Page<Lab> getAll(int page, int size, String sort, String direction) {
    Sort sorting =
        Sort.by(
            Sort.Direction.fromString(direction.equalsIgnoreCase("desc") ? "desc" : "asc"), sort);
    Pageable pageable = PageRequest.of(page, size, sorting);
    return labRepository.findAll(pageable);
  }

  @Override
  @Transactional
  public void deleteLab(UUID labId) {
    Lab lab = labRepository.findById(labId).orElseThrow(() -> new LabException("Invalid Lab Id"));
    List<LabTest> labTests = lab.getTests();
    imageService.delete(lab.getPublicId());
    labTests.forEach(labTest -> labTest.setLab(null));
    labRepository.delete(lab);
  }

  @Override
  @Transactional
  public void updateLab(String name, List<Address> entity, MultipartFile file, UUID labId)
      throws IOException {
    Lab lab = labRepository.findById(labId).orElseThrow(() -> new LabException("Invalid Lab Id"));
    imageService.delete(lab.getPublicId());
    setLogo(file, lab);
    lab.setName(name);
    addressService.deleteAll(lab.getAddresses());
    lab.setAddresses(addressService.saveAll(entity));
    labRepository.save(lab);
  }

  private void setLogo(MultipartFile file, Lab lab) throws IOException {
    UploadResults result = imageService.uploadResource(file, UploadType.IMAGE);
    lab.setLogo(result.url());
    lab.setPublicId(result.publicId());
  }

  @Override
  public Optional<Lab> getLabById(UUID id) {
    return labRepository.findById(id);
  }

  @Override
  public Lab save(Lab lab) {
    return labRepository.save(lab);
  }
}
