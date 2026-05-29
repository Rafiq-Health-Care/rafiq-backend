package com.nexaworks.rafiq.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.feedback.FeedbackResponse;
import com.nexaworks.rafiq.entities.Feedback;
import com.nexaworks.rafiq.entities.Patient;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "comment", source = "feedback")
    @Mapping(target = "patientName", expression = "java(buildPatientName(feedback.getPatient()))")
    @Mapping(target = "createdAt", expression = "java(toLocalDate(feedback.getCreatedAt()))")
    FeedbackResponse toResponse(Feedback feedback);

    List<FeedbackResponse> toResponseList(List<Feedback> feedbacks);

    default String buildPatientName(Patient patient) {
        if (patient == null) {
            return "";
        }
        String firstName = patient.getFirstName() == null ? "" : patient.getFirstName().trim();
        String lastName = patient.getLastName() == null ? "" : patient.getLastName().trim();
        return (firstName + " " + lastName).trim();
    }

    default LocalDate toLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
