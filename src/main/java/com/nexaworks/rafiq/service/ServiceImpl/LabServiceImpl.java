package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.Address;
import com.nexaworks.rafiq.entities.Lab;
import com.nexaworks.rafiq.repository.LabRepository;
import com.nexaworks.rafiq.service.AddressService;
import com.nexaworks.rafiq.service.ImageService;
import com.nexaworks.rafiq.service.LabService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
        List<Address> addresses = addressService.saveAll(entity);
        Lab lab = new Lab();
        lab.setName(name);
        lab.setAddresses(addresses);
        String logo = imageService.uploadFile(file);
        lab.setLogo(logo);
        labRepository.save(lab);
    }
}
