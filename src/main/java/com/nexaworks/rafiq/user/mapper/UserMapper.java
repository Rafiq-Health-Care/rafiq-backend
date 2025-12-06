package com.nexaworks.rafiq.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.doctor.entity.model.Doctor;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.user.api.dto.request.PatientRegistrationRequest;
import com.nexaworks.rafiq.user.entity.enums.Gender;

import jakarta.validation.Valid;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gender", expression = "java(parseGender(request.gender()))")
    Patient toUser(PatientRegistrationRequest request);

    default Gender parseGender(String gender) {
        if (gender == null)
            return null;
        String g = gender.trim().toLowerCase();
        if ("male".equals(g))
            return Gender.MALE;
        if ("female".equals(g))
            return Gender.FEMALE;
        return null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gender", expression = "java(parseGender(user.gender()))")
    Doctor toDoctor(@Valid PatientRegistrationRequest user);
}
