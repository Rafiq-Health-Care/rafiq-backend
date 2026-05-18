package com.nexaworks.rafiq.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.EditConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.ScheduleResponse;
import com.nexaworks.rafiq.entities.ConsultationSlot;

@Mapper(componentModel = "spring")
public interface ConsultationSlotMapper {

    @Mapping(target = "slotId", source = "id")
    @Mapping(target = "duration", source = "durationMinutes")
    EditConsultationSlotResponse toEditResponse(ConsultationSlot slot);

    default PageResponse<ScheduleResponse> toSchedulePageResponse(Page<ConsultationSlot> slotPage) {
        return new PageResponse<>(slotPage.getContent().stream().map(this::toScheduleDto).toList(),
                slotPage.getNumberOfElements(), slotPage.getSize(), slotPage.getTotalPages(),
                slotPage.isLast(), slotPage.isFirst());
    }
    @Mapping(target = "slotId", source = "id")
    @Mapping(target = "consultationId", expression = "java(getConsultationId(slot))")
    @Mapping(target = "patientName", expression = "java(getPatientName(slot))")
    ScheduleResponse toScheduleDto(ConsultationSlot slot);

    default UUID getConsultationId(ConsultationSlot slot) {
        return slot.getConsultation() != null ? slot.getConsultation().getId() : null;
    }

    default String getPatientName(ConsultationSlot slot) {
        if (slot.getConsultation() == null)
            return null;
        if (slot.getConsultation().getPatient() == null)
            return null;
        return slot.getConsultation().getPatient().getName();
    }
}
