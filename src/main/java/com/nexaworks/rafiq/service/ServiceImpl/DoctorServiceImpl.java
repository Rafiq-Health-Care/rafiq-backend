package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Status;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.DoctorService;
import com.nexaworks.rafiq.service.SpecializationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final SpecializationService specializationService;

    @Override
    @Transactional
    public DoctorProfile createProfile(User doctor, String description, UUID specialization, String id, String logo) {
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setUser(doctor);
        doctorProfile.setDescription(description);
        doctorProfile.setSpecialization(specializationService.getSpecialization(specialization));
        doctorProfile.setNationalId(id);
        doctorProfile.setPublicId(logo);
        doctorProfile.setStatus(Status.IN_REVIEW);
        return doctorRepository.save(doctorProfile);
    }
}
