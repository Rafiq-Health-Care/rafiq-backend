package com.nexaworks.rafiq.doctor.service.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.doctor.entity.model.Doctor;
import com.nexaworks.rafiq.doctor.entity.model.Specialization;
import com.nexaworks.rafiq.doctor.repository.DoctorRepository;
import com.nexaworks.rafiq.doctor.service.DoctorService;
import com.nexaworks.rafiq.doctor.service.SpecializationService;
import com.nexaworks.rafiq.shared.entity.FileCategory;
import com.nexaworks.rafiq.shared.event.doctor.DoctorRegisterEvent;
import com.nexaworks.rafiq.shared.event.doctor.UploadFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final SpecializationService specializationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void register(DoctorRegisterEvent event) {
        Specialization doctorSpecialization = specializationService
                .getSpecialization(event.specializationId());
        Doctor doctor = Doctor.builder().specialization(doctorSpecialization)
                .firstName(event.basicInfo().firstName()).lastName(event.basicInfo().lastName())
                .email(event.basicInfo().email()).id(event.basicInfo().userId()).build();
        doctorRepository.save(doctor);
        eventPublisher.publishEvent(new UploadFile(event.nationalId(), event.basicInfo().userId(),
                FileCategory.NATIONAL_ID));
        log.info("Doctor registered successfully");
    }
}
