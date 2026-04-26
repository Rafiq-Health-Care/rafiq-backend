package com.nexaworks.rafiq.dto.response.doctor;

import com.nexaworks.rafiq.entities.enums.Specialization;

import java.util.UUID;

public record DoctorDto(UUID id, String firstName, String lastName, Specialization specialization) {
}
