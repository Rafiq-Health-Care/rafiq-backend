package com.nexaworks.rafiq.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.consultation.ConsultationResponse;
import com.nexaworks.rafiq.dto.response.consultation.PatientConsultationResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorDto;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSummary;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.User;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {
    @Mapping(target = "consultationId", source = "id")
    @Mapping(target = "slotId", source = "slot.id")
    @Mapping(target = "startTime", source = "slot.startTime")
    @Mapping(target = "durationInMinutes", source = "slot.durationMinutes")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "price", expression = "java(getPrice(consultation))")
    @Mapping(target = "doctor", expression = "java(getDoctorDto(consultation))")
    @Mapping(target = "bookedAt", expression = "java(toLocalDateTime(consultation.getCreatedAt()))")
    @Mapping(target = "cancelledAt", expression = "java(getCancelledAt(consultation))")
    @Mapping(target = "reason", expression = "java(getCancellationReason(consultation))")
    @Mapping(target = "cancelByPatient", expression = "java(isCancelledByPatient(consultation))")
    @Mapping(target = "rate", expression = "java(getRate(consultation.getDoctor()))")
    @Mapping(target = "reviewCount", expression = "java(consultation.getDoctor().getFeedbackCount())")
    ConsultationResponse toDto(Consultation consultation);

    @Mapping(target = "consultationId", source = "id")
    @Mapping(target = "doctorName", expression = "java(getDoctorName(consultation))")
    @Mapping(target = "doctorBio", expression = "java(getDoctorBio(consultation))")
    @Mapping(target = "doctorImage", expression = "java(getDoctorImage(consultation))")
    @Mapping(target = "startTime", source = "slot.startTime")
    @Mapping(target = "duration", source = "slot.durationMinutes")
    @Mapping(target = "summaryId", expression = "java(getSummaryId(consultation))")
    @Mapping(target = "doctorId", expression = "java(getDoctorId(consultation))")
    PatientConsultationResponse toPatientResponse(Consultation consultation);

    default LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    default LocalDateTime getCancelledAt(Consultation consultation) {
        if (consultation.getCancellationLog() == null) {
            return null;
        }
        return toLocalDateTime(consultation.getCancellationLog().getCreatedAt());
    }

    default String getCancellationReason(Consultation consultation) {
        return consultation.getCancellationLog() == null
                ? null
                : consultation.getCancellationLog().getReason();
    }

    default boolean isCancelledByPatient(Consultation consultation) {
        if (consultation.getCancellationLog() == null) {
            return false;
        }
        User cancelledBy = consultation.getCancellationLog().getCancelledBy();
        return cancelledBy != null && consultation.getPatient() != null
                && cancelledBy.getId().equals(consultation.getPatient().getId());
    }

    default BigDecimal getPrice(Consultation consultation) {
        if (consultation.getPayment() != null) {
            return consultation.getPayment().getAmount();
        }
        Doctor doctor = consultation.getDoctor();
        return doctor == null ? null : doctor.getPrice();
    }

    default DoctorDto getDoctorDto(Consultation consultation) {
        Doctor doctor = consultation.getDoctor();
        if (doctor == null) {
            return null;
        }
        return new DoctorDto(doctor.getId(), doctor.getFirstName(), doctor.getLastName(),
                doctor.getSpecialization());
    }

    default String getDoctorName(Consultation consultation) {
        Doctor doctor = consultation.getDoctor();
        return doctor == null ? null : doctor.getName();
    }

    default String getDoctorBio(Consultation consultation) {
        Doctor doctor = consultation.getDoctor();
        return doctor == null ? null : doctor.getBiography();
    }

    default String getDoctorImage(Consultation consultation) {
        Doctor doctor = consultation.getDoctor();
        return doctor == null ? null : doctor.getPersonalPhoto();
    }

    default UUID getSummaryId(Consultation consultation) {
        ConsultationSummary summary = consultation.getConsultationSummary();
        return summary == null ? null : summary.getId();
    }

    default UUID getDoctorId(Consultation consultation) {
        Doctor doctor = consultation.getDoctor();
        return doctor == null ? null : doctor.getId();
    }
    default double getRate(Doctor doctor) {
        return doctor.getRating().doubleValue();
    }
}
