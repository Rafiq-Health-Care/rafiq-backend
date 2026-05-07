package com.nexaworks.rafiq.dto.response.doctor;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Specialization;

public record DoctorDto(UUID id, String firstName, String lastName, Specialization specialization) {
}
