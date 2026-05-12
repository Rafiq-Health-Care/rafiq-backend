package com.nexaworks.rafiq.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationResponse;
import com.nexaworks.rafiq.dto.response.consultation.PatientConsultationResponse;
import com.nexaworks.rafiq.dto.response.consultation.ScheduleResponse;
import com.nexaworks.rafiq.entities.Consultation;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {
    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "duration", source = "timeSlot.durationMinutes")
    @Mapping(target = "price", source = "doctor.price")
    @Mapping(target = "bookedAt", source = "payment.createdAt")
    @Mapping(target = "cancelledAt", source = "cancellationLog.createdAt", qualifiedByName = "instantToLocalDateTime")
    @Mapping(target = "reason", source = "cancellationLog.reason")
    @Mapping(target = "cancelByPatient", expression = "java(com.nexaworks.rafiq.mapper.ConsultationMapper.isCancelledByPatient(consultation))")
    ConsultationResponse toDto(Consultation consultation);

    @Named("instantToLocalDateTime")
    default LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    static boolean isCancelledByPatient(Consultation consultation) {
        if (consultation.getCancellationLog() == null
                || consultation.getCancellationLog().getCancelledBy() == null
                || consultation.getPatient() == null) {
            return false;
        }
        return consultation.getCancellationLog().getCancelledBy().getId()
                .equals(consultation.getPatient().getId());
    }

    @Mapping(target = "timeSlot.startTime", source = "startTime")
    @Mapping(target = "timeSlot.durationMinutes", source = "duration")
    Consultation toEntity(AddConsultationRequest request);

    default PageResponse<ConsultationResponse> toPageResponse(Page<Consultation> page) {
        return new PageResponse<>(page.getContent().stream().map(this::toDto).toList(),
                page.getNumberOfElements(), page.getSize(), page.getTotalPages(), page.isLast(),
                page.isFirst());
    }

    default List<ConsultationResponse> toDtoList(List<Consultation> upcomingConsultation) {
        return upcomingConsultation.stream().map(this::toDto).toList();
    }

    default PageResponse<ScheduleResponse> toSchedulePageResponse(
            Page<Consultation> consultations) {
        return new PageResponse<>(
                consultations.getContent().stream().map(this::toScheduleDto).toList(),
                consultations.getNumberOfElements(), consultations.getSize(),
                consultations.getTotalPages(), consultations.isLast(), consultations.isFirst());
    }

    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "duration", source = "timeSlot.durationMinutes")
    @Mapping(target = "patientName", expression = "java(consultation.getPatient() != null ? consultation.getPatient().getName() : null)")
    ScheduleResponse toScheduleDto(Consultation consultation);

    @Mapping(target = "doctorName", expression = "java(consultation.getDoctor() != null ? consultation.getDoctor().getName() : null)")
    @Mapping(target = "doctorBio", source = "doctor.description")
    @Mapping(target = "doctorImage", source = "doctor.personalPhoto")
    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "duration", source = "timeSlot.durationMinutes")
    @Mapping(target = "summaryId", expression = "java(consultation.getSummary() != null ? consultation.getSummary().getId() : null)")
    PatientConsultationResponse toPatientDto(Consultation consultation);

    default List<PatientConsultationResponse> toPatientDtoList(List<Consultation> consultations) {
        return consultations.stream().map(this::toPatientDto).toList();
    }
}
