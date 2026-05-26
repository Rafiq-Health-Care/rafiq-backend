package com.nexaworks.rafiq.dto.response.doctor;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nexaworks.rafiq.entities.enums.Specialization;

@JsonDeserialize
public record DoctorDto(UUID id, String firstName, String lastName,
        Specialization specialization) implements Serializable {
}
