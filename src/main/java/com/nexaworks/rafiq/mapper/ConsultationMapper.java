package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {
    @Mapping(target = "startTime",source = "timeSlot.startTime")
    @Mapping(target = "date",source = "timeSlot.date")
    @Mapping(target = "duration",source = "timeSlot.durationMinutes")
    ConsultationResponse toDto(Consultation consultation);

    @Mapping(target = "timeSlot.startTime",source = "startTime")
    @Mapping(target = "timeSlot.date",source = "date")
    @Mapping(target = "timeSlot.durationMinutes",source = "duration")
    Consultation toEntity(AddConsultationRequest request);


    default PageResponse<ConsultationResponse> toPageResponse(Page<Consultation> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(this::toDto)
                        .toList(),
                page.getNumberOfElements(),
                page.getSize(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }
}
