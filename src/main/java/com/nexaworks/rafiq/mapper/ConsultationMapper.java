package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {
    ConsultationResponse toDto(Consultation consultation);

    Consultation toEntity(AddConsultationRequest request);

    List<ConsultationResponse> toListDto(List<Consultation> consultations);
}
