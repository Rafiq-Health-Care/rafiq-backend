package com.nexaworks.rafiq.shared.event.doctor;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.shared.event.patient.PatientRegistrationEvent;

public record DoctorRegisterEvent(PatientRegistrationEvent basicInfo, UUID doctorId,
        MultipartFile nationalId, UUID specializationId) {
}
