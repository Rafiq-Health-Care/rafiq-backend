package com.nexaworks.rafiq.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.consultation.ConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.EditConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.ScheduleResponse;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientDto;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

@Mapper(componentModel = "spring")
public interface ConsultationSlotMapper {

    @Mapping(target = "slotId", source = "id")
    @Mapping(target = "duration", source = "durationMinutes")
    @Mapping(target = "createdAt", expression = "java(toLocalDateTime(slot.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toLocalDateTime(slot.getUpdatedAt()))")
    EditConsultationSlotResponse toEditResponse(ConsultationSlot slot);

    @Mapping(target = "slotId", source = "id")
    @Mapping(target = "durationInMinutes", source = "durationMinutes")
    @Mapping(target = "consultationId", expression = "java(getConsultationId(slot))")
    @Mapping(target = "patientName", expression = "java(getPatientName(slot))")
    ScheduleResponse toScheduleDto(ConsultationSlot slot);

    default Consultation getActiveConsultation(ConsultationSlot slot) {
        if (slot.getConsultations() == null) {
            return null;
        }
        return slot.getConsultations().stream()
                .filter(consultation -> consultation.getStatus() != ConsultationStatus.CANCELLED)
                .findFirst().orElse(null);
    }

    default Consultation getConsultationWithCancellation(ConsultationSlot slot) {
        if (slot.getConsultations() == null) {
            return null;
        }
        return slot.getConsultations().stream()
                .filter(consultation -> consultation.getCancellationLog() != null).findFirst()
                .orElse(null);
    }

    default UUID getConsultationId(ConsultationSlot slot) {
        Consultation consultation = getActiveConsultation(slot);
        return consultation == null ? null : consultation.getId();
    }

    default String getPatientName(ConsultationSlot slot) {
        Consultation consultation = getActiveConsultation(slot);
        if (consultation == null || consultation.getPatient() == null) {
            return null;
        }
        return consultation.getPatient().getFirstName() + " "
                + consultation.getPatient().getLastName();
    }

    @Mapping(target = "consultationId", expression = "java(getConsultationId(slot))")
    @Mapping(target = "slotId", source = "id")
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
        Consultation consultation = getActiveConsultation(slot);
        return consultation == null ? null : toLocalDateTime(consultation.getCreatedAt());
    }

    default LocalDateTime getCancelledAt(ConsultationSlot slot) {
        Consultation consultation = getConsultationWithCancellation(slot);
        if (consultation == null) {
            return null;
        }
        return toLocalDateTime(consultation.getCancellationLog().getCreatedAt());
    }

    default String getCancellationReason(ConsultationSlot slot) {
        Consultation consultation = getConsultationWithCancellation(slot);
        return consultation == null ? null : consultation.getCancellationLog().getReason();
    }

    default boolean isCancelledByPatient(ConsultationSlot slot) {
        Consultation consultation = getConsultationWithCancellation(slot);
        if (consultation == null) {
            return false;
        }
        User cancelledBy = consultation.getCancellationLog().getCancelledBy();
        return cancelledBy != null && consultation.getPatient() != null
                && cancelledBy.getId().equals(consultation.getPatient().getId());
    }

    default ConsultationStatus getConsultationStatus(ConsultationSlot slot) {
        Consultation consultation = getActiveConsultation(slot);
        return consultation == null ? null : consultation.getStatus();
    }

    default BigDecimal getPrice(ConsultationSlot slot) {
        Consultation consultation = getActiveConsultation(slot);
        if (consultation != null && consultation.getPayment() != null) {
            return consultation.getPayment().getAmount();
        }
        return slot.getDoctor() == null ? null : slot.getDoctor().getPrice();
    }

    default PatientDto getPatientDto(ConsultationSlot slot) {
        Consultation consultation = getActiveConsultation(slot);
        if (consultation == null || consultation.getPatient() == null) {
            return null;
        }
        return new PatientDto(consultation.getPatient().getId(),
                consultation.getPatient().getFirstName(), consultation.getPatient().getLastName());
    }
}
