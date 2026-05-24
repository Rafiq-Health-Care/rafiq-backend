package com.nexaworks.rafiq.service.lab;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.dto.request.lab.AddLabRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.lab.LabResponse;
import com.nexaworks.rafiq.entities.Address;
import com.nexaworks.rafiq.entities.Lab;
import com.nexaworks.rafiq.entities.enums.UploadType;
import com.nexaworks.rafiq.exception.custom.labtest.LabException;
import com.nexaworks.rafiq.mapper.AddressMapper;
import com.nexaworks.rafiq.mapper.LabMapper;
import com.nexaworks.rafiq.repository.LabRepository;
import com.nexaworks.rafiq.service.file.ImageService;
import com.nexaworks.rafiq.service.user.AddressService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class LabServiceImpl implements LabService {
    private final LabRepository labRepository;
    private final AddressService addressService;
    private final ImageService imageService;
    private final LabMapper labMapper;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public void addLab(AddLabRequest request, MultipartFile file) throws IOException {
        List<Address> entity = addressMapper.toEntity(request.addresses());
        Lab lab = new Lab();
        lab.setName(request.name());
        setLogo(file, lab);
        labRepository.save(lab);
        entity.forEach(e -> e.setLab(lab));
        addressService.saveAll(entity);
    }

    @Override
    public PageResponse<LabResponse> getAll(int page, int size, String sort, String direction) {
        Sort sorting = Sort.by(
                Sort.Direction.fromString(direction.equalsIgnoreCase("desc") ? "desc" : "asc"),
                sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Lab> labs = labRepository.findAll(pageable);
        return PageResponse.of(labs, labMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteLab(UUID labId) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new LabException("Invalid Lab Id"));
        imageService.delete(lab.getPublicId());
        labRepository.delete(lab);
    }

    @Override
    @Transactional
    public void updateLab(AddLabRequest request, MultipartFile file, UUID labId)
            throws IOException {
        List<Address> entity = addressMapper.toEntity(request.addresses());
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new LabException("Invalid Lab Id"));
        imageService.delete(lab.getPublicId());
        setLogo(file, lab);
        lab.setName(request.name());
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
