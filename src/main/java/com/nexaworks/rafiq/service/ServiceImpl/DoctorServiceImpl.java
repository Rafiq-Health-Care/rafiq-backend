package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.Status;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.DoctorService;
import com.nexaworks.rafiq.service.SpecializationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final SpecializationService specializationService;

    @Override
    @Transactional
    public Doctor createProfile(User user, String description, UUID specialization) {
        Doctor doctor = new Doctor();
        // Copy User properties to Doctor (since Doctor extends User)
        doctor.setId(user.getId());
        doctor.setEmail(user.getEmail());
        doctor.setPassword(user.getPassword());
        doctor.setFirstName(user.getFirstName());
        doctor.setLastName(user.getLastName());
        doctor.setPhone(user.getPhone());
        doctor.setBirthDate(user.getBirthDate());
        doctor.setActive(user.isActive());
        doctor.setLocked(user.isLocked());
        doctor.setEnabled(user.isEnabled());
        doctor.setNotificationToken(user.getNotificationToken());
        doctor.setGender(user.getGender());
        doctor.setRoles(user.getRoles());
        // Audit fields are inherited from BaseEntity and handled automatically

        // Set Doctor-specific properties
        doctor.setDescription(description);
        doctor.setSpecialization(specializationService.getSpecialization(specialization));
        doctor.setStatus(Status.IN_REVIEW);

        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void updateNationalId(UploadResults uploadResults, UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(
                () -> new UserNotFoundException("Doctor not found with id: " + doctorId));
        doctor.setNationalId(uploadResults.url());
        doctor.setPublicId(uploadResults.publicId());
    }
}
