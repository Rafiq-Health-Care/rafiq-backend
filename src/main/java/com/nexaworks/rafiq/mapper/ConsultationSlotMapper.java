package com.nexaworks.rafiq.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.dto.response.consultation.EditConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.ScheduleResponse;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientDto;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.User;

@Mapper(componentModel = "spring")
public interface ConsultationSlotMapper {

    @Mapping(target = "slotId", source = "id")
    @Mapping(target = "duration", source = "durationMinutes")
    @Mapping(target = "createdAt", expression = "java(toLocalDateTime(slot.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toLocalDateTime(slot.getUpdatedAt()))")
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

    default PageResponse<DoctorConsultationResponse> toDoctorPageResponse(
            Page<DoctorConsultationResponse> slots) {
        return new PageResponse<>(slots.getContent(), slots.getNumberOfElements(), slots.getSize(),
                slots.getTotalPages(), slots.isLast(), slots.isFirst());
    }

    default PageResponse<ConsultationSlotResponse> toPageResponse(Page<ConsultationSlot> upcoming) {
        return new PageResponse<>(upcoming.getContent().stream().map(this::toDto).toList(),
                upcoming.getNumberOfElements(), upcoming.getSize(), upcoming.getTotalPages(),
                upcoming.isLast(), upcoming.isFirst());
    }

    @Mapping(target = "consultationId", expression = "java(getConsultationId(slot))")
    @Mapping(target = "slotId", source = "id")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "durationInMinutes", source = "durationMinutes")
    @Mapping(target = "status", expression = "java(getConsultationStatus(slot))")
    @Mapping(target = "price", expression = "java(getPrice(slot))")
    @Mapping(target = "patient", expression = "java(getPatientDto(slot))")
    @Mapping(target = "bookedAt", expression = "java(getBookedAt(slot))")
    @Mapping(target = "cancelledAt", expression = "java(getCancelledAt(slot))")
    @Mapping(target = "reason", expression = "java(getCancellationReason(slot))")
    @Mapping(target = "cancelByPatient", expression = "java(isCancelledByPatient(slot))")
    ConsultationSlotResponse toDto(ConsultationSlot slot);

    default LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    default LocalDateTime getBookedAt(ConsultationSlot slot) {
        Consultation consultation = slot.getConsultation();
        return consultation == null ? null : toLocalDateTime(consultation.getCreatedAt());
    }

    default LocalDateTime getCancelledAt(ConsultationSlot slot) {
        Consultation consultation = slot.getConsultation();
        if (consultation == null || consultation.getCancellationLog() == null) {
            return null;
        }
        return toLocalDateTime(consultation.getCancellationLog().getCreatedAt());
    }

    default String getCancellationReason(ConsultationSlot slot) {
        Consultation consultation = slot.getConsultation();
        return consultation == null || consultation.getCancellationLog() == null
                ? null
                : consultation.getCancellationLog().getReason();
    }

    default boolean isCancelledByPatient(ConsultationSlot slot) {
        Consultation consultation = slot.getConsultation();
        if (consultation == null || consultation.getCancellationLog() == null) {
            return false;
        }
        User cancelledBy = consultation.getCancellationLog().getCancelledBy();
        return cancelledBy != null && consultation.getPatient() != null
                && cancelledBy.getId().equals(consultation.getPatient().getId());
    }

    default com.nexaworks.rafiq.entities.enums.ConsultationStatus getConsultationStatus(
            ConsultationSlot slot) {
        return slot.getConsultation() == null ? null : slot.getConsultation().getStatus();
    }

    default BigDecimal getPrice(ConsultationSlot slot) {
        Consultation consultation = slot.getConsultation();
        if (consultation != null && consultation.getPayment() != null) {
            return consultation.getPayment().getAmount();
        }
        return slot.getDoctor() == null ? null : slot.getDoctor().getPrice();
    }

    default PatientDto getPatientDto(ConsultationSlot slot) {
        Consultation consultation = slot.getConsultation();
        if (consultation == null || consultation.getPatient() == null) {
            return null;
        }
        return new PatientDto(consultation.getPatient().getId(),
                consultation.getPatient().getFirstName(), consultation.getPatient().getLastName());
    }
}
