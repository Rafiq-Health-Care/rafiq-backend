package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.response.consultation.ConsultationCreatedResponse;
import com.nexaworks.rafiq.entities.Consultation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {
    ConsultationCreatedResponse toDto(Consultation consultation);
}
