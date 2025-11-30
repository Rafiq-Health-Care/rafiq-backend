package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;
import com.nexaworks.rafiq.entities.PatientProfile;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phone", target = "phoneNumber")
    @Mapping(source = "id", target = "patientId")
    @Mapping(target = "bmi", expression = "java(getBmiStatus(user.getWeight(), user.getHeight()))")
    PatientProfileResponse toResponse(PatientProfile patient);

    default String getBmiStatus(Double weight, Double height) {
        int bmi = (int) (weight / (height * height));
        return bmi < 18.5 ? "Underweight" : bmi >= 18.5 && bmi < 25 ? "Normal" : "Overweight";
    }
}
