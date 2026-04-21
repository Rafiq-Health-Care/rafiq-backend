package com.nexaworks.rafiq.dto.response.doctor;

import java.util.UUID;

public record DoctorDto(UUID id, String firstName, String lastName,String specialization) {
}
