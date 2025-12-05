package com.nexaworks.rafiq.user.event;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public record DoctorRegisterEvent(PatientRegistrationEvent event, UUID doctorId,
        MultipartFile nationalId) {
}
