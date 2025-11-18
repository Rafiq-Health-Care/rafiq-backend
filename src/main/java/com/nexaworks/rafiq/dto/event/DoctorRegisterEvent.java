package com.nexaworks.rafiq.dto.event;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public record DoctorRegisterEvent(UserRegistrationEvent event, UUID doctorId,
        MultipartFile nationalId) {
}
