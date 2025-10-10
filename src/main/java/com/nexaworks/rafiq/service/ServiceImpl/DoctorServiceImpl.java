package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    @Override
    public DoctorProfile createProfile(User doctor) {
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setUser(doctor);
        return doctorRepository.save(doctorProfile);
    }
}
