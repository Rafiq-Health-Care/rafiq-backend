package com.nexaworks.rafiq.mapper;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {
    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "duration", source = "timeSlot.durationMinutes")
    ConsultationResponse toDto(Consultation consultation);

    @Mapping(target = "timeSlot.startTime", source = "startTime")
    @Mapping(target = "timeSlot.durationMinutes", source = "duration")
    @Mapping(target = "price", source = "price")
    Consultation toEntity(AddConsultationRequest request);

    default PageResponse<ConsultationResponse> toPageResponse(Page<Consultation> page) {
        return new PageResponse<>(page.getContent().stream().map(this::toDto).toList(),
                page.getNumberOfElements(), page.getSize(), page.getTotalPages(), page.isLast(),
                page.isFirst());
    }

    @IterableMapping(elementTargetType = ConsultationResponse.class)
    List<ConsultationResponse> toDtoList(List<Consultation> upcomingConsultation);
}
